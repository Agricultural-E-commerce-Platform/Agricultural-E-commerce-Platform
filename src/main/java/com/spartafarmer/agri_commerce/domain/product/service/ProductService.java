package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 목록 조회
    public Page<ProductListResponse> getProducts(ProductType type, Pageable pageable) {
        Page<Product> products;

        // type이 없으면 전체 상품 목록 조회
        if (type == null) {
            products = productRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        // type이 있으면 해당 타입(NORMAL / SPECIAL)만 조회
        else {
            products = productRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        }

        // 응답 DTO로 변환
        return products.map(product -> new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getType(),
                product.getNormalPrice(),
                product.getSalePrice(),
                product.getSpecialPrice(),
                product.getStock(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt()));
    }

    // 상품 상세 조회
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

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
