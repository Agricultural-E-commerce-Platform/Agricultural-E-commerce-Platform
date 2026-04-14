package com.spartafarmer.agri_commerce.domain.cart.dto;

import lombok.Getter;

import java.util.List;

// 장바구니 조회 dto

@Getter
public class CartResponse {
    private final List<CartItemResponse> cartItems;
    private final Long totalPrice;
    private final boolean isMinOrderAmountMet; // 최소 주문금액을 충족했는가?
    private final Long minOrderAmount;

    public CartResponse(List<CartItemResponse> cartItems, Long totalPrice, boolean isMinOrderAmountMet) {
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
        this.isMinOrderAmountMet = isMinOrderAmountMet;
        this.minOrderAmount = 20000L; // 최소 주문 금액 정책
    }
}
