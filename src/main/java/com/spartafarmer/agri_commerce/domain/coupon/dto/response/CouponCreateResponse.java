package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponCreateResponse {

    private final Long couponId;
    private final String name;
    private final Long discountAmount;
    private final int totalQuantity;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private CouponCreateResponse(Coupon coupon) {
        this.couponId = coupon.getId();
        this.name = coupon.getName();
        this.discountAmount = coupon.getDiscountAmount();
        this.totalQuantity = coupon.getTotalQuantity();
        this.startTime = coupon.getStartTime();
        this.endTime = coupon.getEndTime();
    }

    public static CouponCreateResponse from(Coupon coupon) {
        return new CouponCreateResponse(coupon);
    }
}
