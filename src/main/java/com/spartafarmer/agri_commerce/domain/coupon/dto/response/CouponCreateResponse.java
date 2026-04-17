package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponCreateResponse(
        Long couponId,
        String name,
        Long discountAmount,
        int totalQuantity,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static CouponCreateResponse from(Coupon coupon) {
        return new CouponCreateResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountAmount(),
                coupon.getTotalQuantity(),
                coupon.getStartTime(),
                coupon.getEndTime()
        );
    }
}
