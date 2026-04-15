package com.spartafarmer.agri_commerce.domain.coupon.dto.response;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCouponResponse {

    private final Long userCouponId;
    private final String couponName;
    private final Long discountAmount;
    private final LocalDateTime expiredAt;
    private final CouponStatus status;

    private UserCouponResponse(UserCoupon userCoupon) {
        this.userCouponId = userCoupon.getId();
        this.couponName = userCoupon.getCoupon().getName();
        this.discountAmount = userCoupon.getCoupon().getDiscountPrice();
        this.expiredAt = userCoupon.getExpiredAt();
        this.status = userCoupon.getStatus();
    }

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(userCoupon);
    }
}
