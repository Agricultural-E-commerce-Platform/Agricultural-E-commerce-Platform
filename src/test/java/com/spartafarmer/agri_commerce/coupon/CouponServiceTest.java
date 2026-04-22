package com.spartafarmer.agri_commerce.coupon;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.lock.LockService;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponIssueService;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private LockService lockService;

    @Mock
    private CouponIssueService couponIssueService;

    @Nested
    @DisplayName("쿠폰 생성 테스트")
    class CreateCoupon {

        @Test
        @DisplayName("성공: 올바른 요청 데이터로 쿠폰을 생성한다.")
        void createCoupon_성공() {
            // given
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = LocalDateTime.now().plusDays(2);
            CouponCreateRequest request = new CouponCreateRequest("할인 쿠폰", 5000L, 100, start, end);

            // when
            CouponCreateResponse response = couponService.createCoupon(request);

            // then
            assertThat(response.name()).isEqualTo("할인 쿠폰");
            assertThat(response.discountAmount()).isEqualTo(5000L);
            verify(couponRepository, times(1)).save(any(Coupon.class));
        }

        @Test
        @DisplayName("실패: 시작 시간이 현재보다 이전이면 예외가 발생한다.")
        void createCoupon_실패_시작_시각_현재_이전() {
            // given
            LocalDateTime start = LocalDateTime.now().minusHours(1); // 과거 시간
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            CouponCreateRequest request = new CouponCreateRequest("과거 쿠폰", 5000L, 100, start, end);

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_START_TIME);
        }

        @Test
        @DisplayName("실패: 종료 시간이 시작 시간보다 이전이면 예외가 발생한다.")
        void createCoupon_실패_종료_시각_시작_시각_이전() {
            // given
            LocalDateTime start = LocalDateTime.now().plusHours(1);
            LocalDateTime end = LocalDateTime.now().minusHours(1); // 시작보다 이전
            CouponCreateRequest request = new CouponCreateRequest("역행 쿠폰", 5000L, 100, start, end);

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_END_TIME);
        }
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 테스트")
    void getMyCoupons_성공() {
        // given
        Long userId = 1L;
        when(userCouponRepository.findByUserIdOrderByExpiredAtAsc(userId)).thenReturn(List.of());

        // when
        couponService.getMyCoupons(userId);

        // then
        verify(userCouponRepository, times(1)).findByUserIdOrderByExpiredAtAsc(userId);
    }


    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCoupon {

        @Test
        @DisplayName("쿠폰 발급 테스트: LockService를 통해 발급 로직이 호출되는지 확인")
        void issueCoupon_LockService_호출_확인() {
            // given
            Long couponId = 1L;
            Long userId = 1L;

            // lockService.executeWithLock이 호출될 때 내부 람다를 실행하도록 설정
            when(lockService.executeWithLock(anyString(), any(), any()))
                    .thenAnswer(invocation -> {
                        // 세 번째 인자인 Supplier(람다)를 가져와서 실행
                        java.util.function.Supplier<?> supplier = invocation.getArgument(2);
                        return supplier.get();
                    });

            // when
            couponService.issueCoupon(couponId, userId);

            // then
            verify(lockService, times(1)).executeWithLock(eq("coupon:issue:" + couponId), any(), any());
            verify(couponIssueService, times(1)).issueCoupon(couponId, userId);
        }
    }
}