package com.spartafarmer.agri_commerce.domain.coupon.repository;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 만료 처리 대상 쿠폰 조회 (endTime 이 지난 것)
    List<Coupon> findAllByEndTimeBefore(LocalDateTime now);
}