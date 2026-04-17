package com.spartafarmer.agri_commerce.domain.order.dto;

public record OrderItemResponse(
        String productName,
        int quantity,
        Long price,
        Long totalPrice
) {
}