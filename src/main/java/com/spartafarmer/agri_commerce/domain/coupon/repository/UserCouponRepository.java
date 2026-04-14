package com.spartafarmer.agri_commerce.domain.coupon.repository;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 중복 발급 확인
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    // 사용자 보유 쿠폰 목록 조회 (만료 임박순 - 주문 시 쿠폰 선택용)
    List<UserCoupon> findByUserIdAndStatusOrderByExpiredAtAsc(Long userId, CouponStatus status);

    // 만료 일괄 처리 대상 조회 (특정 쿠폰의 미사용 쿠폰들)
    List<UserCoupon> findByCouponIdAndStatus(Long couponId, CouponStatus status, Pageable pageable);

    // 단건 조회 (쿠폰 사용 시)
    Optional<UserCoupon> findByIdAndUserId(Long id, Long userId);
}