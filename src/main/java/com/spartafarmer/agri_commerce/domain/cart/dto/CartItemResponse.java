package com.spartafarmer.agri_commerce.domain.cart.dto;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long price,
        int quantity,
        Long totalPrice,
        ProductStatus productStatus
) {
    public CartItemResponse(Long cartItemId, Long productId, String productName, Long price, int quantity, ProductStatus productStatus) {
        this(cartItemId, productId, productName, price, quantity, price * quantity, productStatus);
    }
}