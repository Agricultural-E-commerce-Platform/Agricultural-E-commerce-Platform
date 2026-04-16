package com.spartafarmer.agri_commerce.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderItemResponse {

    private final String productName;
    private final int quantity;
    private final Long price;
    private final Long totalPrice;

    public OrderItemResponse(String productName, int quantity, Long price, Long totalPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }
}
