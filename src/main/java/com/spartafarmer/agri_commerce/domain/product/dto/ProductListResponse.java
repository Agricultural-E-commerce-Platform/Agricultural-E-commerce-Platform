package com.spartafarmer.agri_commerce.domain.product.dto;


import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;

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
    // Product 엔티티를 ProductListResponse로 바꾸는 메서드
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getType(),
                product.getNormalPrice(),
                product.getSalePrice(),
                product.getSpecialPrice(),
                product.getStock(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt()
        );
    }
}
