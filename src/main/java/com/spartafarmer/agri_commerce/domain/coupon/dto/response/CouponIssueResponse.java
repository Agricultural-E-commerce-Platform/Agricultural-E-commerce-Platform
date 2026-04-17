package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;

import java.time.LocalDateTime;

public record CouponIssueResponse (

    Long userCouponId,
    String couponName,
    Long discountAmount,
    CouponStatus status,
    LocalDateTime expiredAt
){
        public static CouponIssueResponse from (UserCoupon userCoupon){
        return new CouponIssueResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getCoupon().getDiscountAmount(),
                userCoupon.getStatus(),
                userCoupon.getExpiredAt()
        );
    }
}
