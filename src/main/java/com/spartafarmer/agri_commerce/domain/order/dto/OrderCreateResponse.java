package com.spartafarmer.agri_commerce.domain.order.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderCreateResponse {

    private final Long orderId;
    private final List<OrderItemResponse> orderItems;

    private final Long originalPrice;
    private final Long discountAmount;
    private final Long finalPrice;

    private final String status;
    private final LocalDateTime orderedAt;

    public OrderCreateResponse(Long orderId,
                               List<OrderItemResponse> orderItems,
                               Long originalPrice,
                               Long discountAmount,
                               Long finalPrice,
                               String status,
                               LocalDateTime orderedAt) {

        this.orderId = orderId;
        this.orderItems = orderItems;
        this.originalPrice = originalPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.status = status;
        this.orderedAt = orderedAt;
    }
}