package com.spartafarmer.agri_commerce.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Long orderId,
        List<OrderItemResponse> orderItems,
        Long originalPrice,
        Long discountAmount,
        Long finalPrice,
        String status,
        LocalDateTime orderedAt
) {
}