package com.spartafarmer.agri_commerce.domain.order.dto;

import com.spartafarmer.agri_commerce.domain.order.entity.OrderItem;

public record OrderItemResponse(
        String productName,
        int quantity,
        Long price,
        Long totalPrice
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getPrice() * orderItem.getQuantity()
        );
    }
}