package com.spartafarmer.agri_commerce.domain.cart.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> cartItems,
        Long totalPrice,
        Long minOrderAmount,
        boolean isMinOrderAmountMet
) {
}