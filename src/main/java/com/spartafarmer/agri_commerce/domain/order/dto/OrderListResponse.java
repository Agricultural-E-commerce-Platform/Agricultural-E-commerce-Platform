package com.spartafarmer.agri_commerce.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderListResponse(
        Long orderId,
        List<OrderItemResponse> orderItems,
        Long finalPrice,
        String status,
        LocalDateTime orderedAt
) {
}