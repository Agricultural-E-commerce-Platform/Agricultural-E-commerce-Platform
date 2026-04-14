package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        return products.map(ProductListResponse::from);
    }

    // 상품 상세 조회
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매 종료 상품
        if (product.getStatus() == ProductStatus.SOLD_OUT ||
                product.getStatus() == ProductStatus.SALE_ENDED) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        return ProductDetailResponse.from(product);
    }
}
