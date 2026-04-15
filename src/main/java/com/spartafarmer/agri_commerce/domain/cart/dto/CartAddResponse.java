package com.spartafarmer.agri_commerce.domain.cart.dto;

import lombok.Getter;

// 장바구니 담기 dto

@Getter
public class CartAddResponse {
    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final Long price;
    private final int quantity;
    private final Long totalPrice;

    public CartAddResponse(Long cartItemId, Long productId, String productName, Long price, int quantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity; // 총 금액 계산
    }
}
