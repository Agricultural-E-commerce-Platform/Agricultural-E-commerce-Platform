package com.spartafarmer.agri_commerce.common;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.lock.LockService;
import com.spartafarmer.agri_commerce.common.lock.RedisLockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedisLockRepository redisLockRepository;

    @InjectMocks
    private LockService lockService;

    @Nested
    @DisplayName("executeWithLock")
    class ExecuteWithLock {

        @Test
        @DisplayName("락 획득 후 작업 성공")
        void executeWithLockSuccess() {
            when(redisLockRepository.tryLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);

            String result = lockService.executeWithLock(
                    "lock:key",
                    Duration.ofSeconds(3),
                    () -> "success"
            );

            assertThat(result).isEqualTo("success");
            verify(redisLockRepository).tryLock(eq("lock:key"), anyString(), any(Duration.class));
            verify(redisLockRepository).unlock(eq("lock:key"), anyString());
        }

        @Test
        @DisplayName("락 획득 실패 시 예외 발생")
        void executeWithLockFailWhenAcquireFails() {
            when(redisLockRepository.tryLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);

            assertThatThrownBy(() ->
                    lockService.executeWithLock("lock:key", Duration.ofSeconds(3), () -> "success")
            ).isInstanceOf(CustomException.class);

            verify(redisLockRepository, never()).unlock(anyString(), anyString());
        }

        @Test
        @DisplayName("작업 중 예외가 나도 unlock 호출")
        void executeWithLockUnlocksOnException() {
            when(redisLockRepository.tryLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);

            assertThatThrownBy(() ->
                    lockService.executeWithLock("lock:key", Duration.ofSeconds(3), () -> {
                        throw new RuntimeException("task fail");
                    })
            ).isInstanceOf(RuntimeException.class);

            verify(redisLockRepository).unlock(eq("lock:key"), anyString());
        }
    }

    @Nested
    @DisplayName("executeWithLocks")
    class ExecuteWithLocks {

        @Test
        @DisplayName("다중 락 획득 후 작업 성공")
        void executeWithLocksSuccess() {
            when(redisLockRepository.tryLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);

            String result = lockService.executeWithLocks(
                    List.of("lock:1", "lock:2"),
                    Duration.ofSeconds(3),
                    () -> "multi-success"
            );

            assertThat(result).isEqualTo("multi-success");
            verify(redisLockRepository, times(2)).tryLock(anyString(), anyString(), any(Duration.class));
            verify(redisLockRepository, times(2)).unlock(anyString(), anyString());
        }

        @Test
        @DisplayName("중간 락 획득 실패 시 이미 획득한 락만 해제")
        void executeWithLocksFailMidway() {
            when(redisLockRepository.tryLock(eq("lock:1"), anyString(), any(Duration.class))).thenReturn(true);
            when(redisLockRepository.tryLock(eq("lock:2"), anyString(), any(Duration.class))).thenReturn(false);

            assertThatThrownBy(() ->
                    lockService.executeWithLocks(
                            List.of("lock:1", "lock:2"),
                            Duration.ofSeconds(3),
                            () -> "fail"
                    )
            ).isInstanceOf(CustomException.class);

            verify(redisLockRepository, times(1)).unlock(eq("lock:1"), anyString());
            verify(redisLockRepository, never()).unlock(eq("lock:2"), anyString());
        }
    }
}
