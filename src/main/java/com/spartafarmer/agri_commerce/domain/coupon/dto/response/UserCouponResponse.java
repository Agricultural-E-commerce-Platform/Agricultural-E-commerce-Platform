package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResponse(
        Long userCouponId,
        String couponName,
        Long discountAmount,
        LocalDateTime expiredAt,
        CouponStatus status
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getCoupon().getDiscountAmount(),
                userCoupon.getExpiredAt(),
                userCoupon.getStatus()
        );
    }
}
