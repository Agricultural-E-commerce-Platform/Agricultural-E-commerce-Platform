package com.spartafarmer.agri_commerce.domain.coupon.repository;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 조건부 UPDATE - 수량 여유가 있을 때만 issuedQuantity 증가
    // affected rows = 1: 성공, 0: 수량 소진
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.issuedQuantity = c.issuedQuantity + 1 " +
            "WHERE c.id = :couponId AND c.issuedQuantity < c.totalQuantity")
    int increaseIssuedQuantityIfAvailable(@Param("couponId") Long couponId);
}