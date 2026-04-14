package com.spartafarmer.agri_commerce.domain.product.dto;


import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;

import java.time.LocalDateTime;

// 상품 목록
public record ProductListResponse(
        Long id,
        String name,
        ProductType type,
        Long normalPrice,
        Long salePrice,
        Long specialPrice,
        Integer stock,
        ProductStatus status,
        String imageUrl,
        LocalDateTime createdAt
) {
}
