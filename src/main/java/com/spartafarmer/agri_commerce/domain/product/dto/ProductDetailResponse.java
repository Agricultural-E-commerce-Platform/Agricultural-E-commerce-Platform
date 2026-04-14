package com.spartafarmer.agri_commerce.domain.product.dto;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;

import java.time.LocalDateTime;

// 상품 상세
public record ProductDetailResponse(
        Long id,
        String name,
        ProductType type,
        ProductStatus status,
        Long normalPrice,
        Long salePrice,
        Long specialPrice,
        Integer stock,
        String imageUrl,
        LocalDateTime createdAt
) {
}
