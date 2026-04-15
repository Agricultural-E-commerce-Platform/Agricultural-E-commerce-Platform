package com.spartafarmer.agri_commerce.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

// 장바구니 담기 dto

@Getter
public class CartAddRequest {
    @NotNull(message = "상품 ID는 필수입니다")
    private  Long productId;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private int quantity;
}
