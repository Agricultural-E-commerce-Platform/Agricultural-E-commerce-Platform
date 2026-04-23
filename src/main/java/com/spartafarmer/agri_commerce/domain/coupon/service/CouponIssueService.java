package com.spartafarmer.agri_commerce.domain.coupon.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponIssueResponse;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    // 기존 레포 3개 그대로 의존성
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    // 락 획득 -> @Transactional -> 로직 -> 커밋 -> 락 해제
    @Transactional
    public CouponIssueResponse issueCoupon(Long couponId, Long userId) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.isAvailableNow(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.COUPON_NOT_AVAILABLE_TIME);
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        if (!coupon.hasRemaining()) {
            throw new CustomException(ErrorCode.COUPON_SOLD_OUT);
        }

        // 모든 검증 통과 후 상태 변경 시작
        // UserCoupon 생성 시 연관관계만 필요하므로 프록시로 조회 (SELECT 쿼리 생략)
        User user = userRepository.getReferenceById(userId);

        coupon.increaseIssuedQuantity();

        UserCoupon userCoupon = UserCoupon.issue(user, coupon, LocalDateTime.now());
        userCouponRepository.save(userCoupon);

        return CouponIssueResponse.from(userCoupon);
    }
}
