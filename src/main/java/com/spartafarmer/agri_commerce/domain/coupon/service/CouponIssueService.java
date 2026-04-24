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

        // 조건부 UPDATE로 수량 증가 (DB 레벨 안전장치)
        // 락 TTL 만료 등 예외 상황에서도 수량 초과 방지
        int updated = couponRepository.increaseIssuedQuantityIfAvailable(couponId);
        if (updated == 0) {
            throw new CustomException(ErrorCode.COUPON_SOLD_OUT);
        }

        // UserCoupon 생성 시 연관관계만 필요하므로 프록시로 조회 (SELECT 쿼리 생략)
        // JWT 인증을 통과한 userId라 존재 검증 불필요
        User user = userRepository.getReferenceById(userId);

        UserCoupon userCoupon = UserCoupon.issue(user, coupon, LocalDateTime.now());
        userCouponRepository.save(userCoupon);

        return CouponIssueResponse.from(userCoupon);
    }
}
