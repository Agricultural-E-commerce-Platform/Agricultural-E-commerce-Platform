package com.spartafarmer.agri_commerce.domain.cart.dto;

public record CartAddResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long price,
        int quantity,
        Long totalPrice
) {
    public CartAddResponse(Long cartItemId, Long productId, String productName, Long price, int quantity) {
        this(cartItemId, productId, productName, price, quantity, price * quantity);
    }
}