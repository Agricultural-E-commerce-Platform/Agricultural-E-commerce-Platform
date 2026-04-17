package com.spartafarmer.agri_commerce.domain.order.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderListResponse {

    private final Long orderId;
    private final List<OrderItemResponse> orderItems;
    private final Long finalPrice;
    private final String status;
    private final LocalDateTime orderedAt;

    public OrderListResponse(Long orderId, List<OrderItemResponse> orderItems, Long finalPrice, String status, LocalDateTime orderedAt) {
        this.orderId = orderId;
        this.orderItems = orderItems;
        this.finalPrice = finalPrice;
        this.status = status;
        this.orderedAt = orderedAt;
    }
}