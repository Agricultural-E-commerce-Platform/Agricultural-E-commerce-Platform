package com.spartafarmer.agri_commerce.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
    // 쿠폰 미사용시 null
    private Long userCouponId;
}
