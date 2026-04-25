package com.spartafarmer.agri_commerce.coupon;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponIssueResponse;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponIssueService;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @InjectMocks
    private CouponIssueService couponIssueService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("성공: 모든 발급 조건(존재 여부, 기간, 중복, 수량)을 만족하면 쿠폰을 발행한다.")
    void issueCoupon_성공() {
        // given
        Long couponId = 1L;
        Long userId = 1L;

        Coupon coupon = mock(Coupon.class);
        User user = mock(User.class);
        UserCoupon userCoupon = mock(UserCoupon.class);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(coupon.isAvailableNow(any())).willReturn(true);
        given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).willReturn(false);
        given(couponRepository.increaseIssuedQuantityIfAvailable(couponId)).willReturn(1);
        given(userRepository.getReferenceById(userId)).willReturn(user);

        given(userCouponRepository.save(any())).willReturn(userCoupon);

        // when
        CouponIssueResponse response = couponIssueService.issueCoupon(couponId, userId);

        // then
        assertThat(response).isNotNull();
        verify(couponRepository).increaseIssuedQuantityIfAvailable(couponId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 쿠폰 ID로 발급 요청 시 예외를 던진다.")
    void issueCoupon_실패_쿠폰_없음() {
        given(couponRepository.findById(any()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                couponIssueService.issueCoupon(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 현재 시간이 쿠폰 발급 가능 기간이 아니면 예외를 던진다.")
    void issueCoupon_실패_발급_기간_아님() {
        Coupon coupon = mock(Coupon.class);

        given(couponRepository.findById(any()))
                .willReturn(Optional.of(coupon));
        given(coupon.isAvailableNow(any())).willReturn(false);

        assertThatThrownBy(() ->
                couponIssueService.issueCoupon(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_AVAILABLE_TIME);
    }

    @Test
    @DisplayName("실패: 이미 해당 쿠폰을 발급받은 유저가 재요청하면 예외를 던진다.")
    void issueCoupon_실패_중복_발급() {
        Coupon coupon = mock(Coupon.class);

        given(couponRepository.findById(any()))
                .willReturn(Optional.of(coupon));
        given(coupon.isAvailableNow(any())).willReturn(true);
        given(userCouponRepository.existsByUserIdAndCouponId(any(), any()))
                .willReturn(true);

        assertThatThrownBy(() ->
                couponIssueService.issueCoupon(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_ALREADY_ISSUED);
    }

    @Test
    @DisplayName("실패: 쿠폰의 잔여 수량이 0이면 예외를 던진다.")
    void issueCoupon_실패_수량_소진() {
        Coupon coupon = mock(Coupon.class);

        given(couponRepository.findById(any()))
                .willReturn(Optional.of(coupon));
        given(coupon.isAvailableNow(any())).willReturn(true);
        given(userCouponRepository.existsByUserIdAndCouponId(any(), any()))
                .willReturn(false);
        given(couponRepository.increaseIssuedQuantityIfAvailable(any())).willReturn(0);

        assertThatThrownBy(() ->
                couponIssueService.issueCoupon(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_SOLD_OUT);
    }

}