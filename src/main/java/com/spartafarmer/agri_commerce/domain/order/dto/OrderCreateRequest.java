package com.spartafarmer.agri_commerce.domain.order.dto;

public record OrderCreateRequest(
        // 쿠폰 미사용시 null
        Long userCouponId
) {
}