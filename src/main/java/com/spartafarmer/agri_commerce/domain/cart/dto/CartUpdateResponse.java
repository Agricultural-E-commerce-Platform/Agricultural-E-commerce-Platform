package com.spartafarmer.agri_commerce.domain.cart.dto;

import lombok.Getter;

// 수량 변경 dto
@Getter
public class CartUpdateResponse {
    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final Long price;
    private final int quantity;
    private final Long totalPrice;

    public CartUpdateResponse(Long cartItemId, Long productId, String productName, Long price, int quantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity;
    }
}
