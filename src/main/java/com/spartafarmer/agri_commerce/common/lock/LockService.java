package com.spartafarmer.agri_commerce.common.lock;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LockService {

    private final RedisLockRepository redisLockRepository;

    // 락을 걸기 → 작업 실행 → 락 풀기
    public <T> T executeWithLock(String key, Duration ttl, Supplier<T> task) {  // <T> 모든 타입 받기 가능
        String uuid = UUID.randomUUID().toString();

        // 락 걸기 시도
        boolean locked = redisLockRepository.tryLock(key, uuid, ttl);
        if (!locked) {
            throw new CustomException(ErrorCode.LOCK_ACQUIRE_FAILED);
        }

        // 락을 건 뒤 실제 작업 실행
        try {
            return task.get();
        } finally {
            // 작업이 끝나거나 예외 처리 되면 락 풀기
            redisLockRepository.unlock(key, uuid);
        }
    }
}
