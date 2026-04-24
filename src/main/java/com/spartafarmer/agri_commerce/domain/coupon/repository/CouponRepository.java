package com.spartafarmer.agri_commerce.domain.coupon.repository;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}