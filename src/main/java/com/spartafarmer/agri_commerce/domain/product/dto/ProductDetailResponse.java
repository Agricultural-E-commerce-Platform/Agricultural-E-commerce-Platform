package com.spartafarmer.agri_commerce.domain.product.dto;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;

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
    // Product 엔티티를 ProductDetailResponse로 바꾸는 메서드
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getType(),
                product.getStatus(),
                product.getNormalPrice(),
                product.getSalePrice(),
                product.getSpecialPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCreatedAt()
        );
    }
}
