package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponListResponse(
        Long couponId,
        String name,
        Long discountAmount,
        int totalQuantity,
        int issuedQuantity,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static CouponListResponse from(Coupon coupon) {
        return new CouponListResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountAmount(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getStartTime(),
                coupon.getEndTime()
        );
    }
}
