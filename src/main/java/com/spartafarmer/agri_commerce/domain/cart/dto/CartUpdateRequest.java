package com.spartafarmer.agri_commerce.domain.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

// 수량 변경 dto

@Getter
public class CartUpdateRequest {
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private int quantity;
}