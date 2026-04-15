package com.spartafarmer.agri_commerce.domain.cart.dto;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import lombok.Getter;

// 장바구니 조회 dto

@Getter
public class CartItemResponse {
    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final Long price;
    private final int quantity;
    private final Long totalPrice;
    private final ProductStatus productStatus;

    public CartItemResponse(Long cartItemId, Long productId, String productName, Long price, int quantity, ProductStatus productStatus) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity;
        this.productStatus = productStatus;
    }
}
