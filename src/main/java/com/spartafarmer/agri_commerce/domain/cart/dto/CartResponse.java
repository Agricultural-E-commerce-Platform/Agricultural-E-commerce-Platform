package com.spartafarmer.agri_commerce.domain.cart.dto;

import lombok.Getter;

import java.util.List;

// 장바구니 조회 dto

@Getter
public class CartResponse {
    private final List<CartItemResponse> cartItems;
    private final Long totalPrice;
    private final Long minOrderAmount;
    private final boolean isMinOrderAmountMet;

    public CartResponse(List<CartItemResponse> cartItems,
                        Long totalPrice,
                        Long minOrderAmount,
                        boolean isMinOrderAmountMet) {
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
        this.minOrderAmount = minOrderAmount;
        this.isMinOrderAmountMet = isMinOrderAmountMet;
    }
}
