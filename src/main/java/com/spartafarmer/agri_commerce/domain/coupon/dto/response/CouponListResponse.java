package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponListResponse {

    private final Long couponId;
    private final String name;
    private final Long discountAmount;
    private final int totalQuantity;
    private final int issuedQuantity;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private CouponListResponse(Coupon coupon) {
        this.couponId = coupon.getId();
        this.name = coupon.getName();
        this.discountAmount = coupon.getDiscountAmount();
        this.totalQuantity = coupon.getTotalQuantity();
        this.issuedQuantity = coupon.getIssuedQuantity();
        this.startTime = coupon.getStartTime();
        this.endTime = coupon.getEndTime();
    }

    public static CouponListResponse from(Coupon coupon) {
        return new CouponListResponse(coupon);
    }
}
