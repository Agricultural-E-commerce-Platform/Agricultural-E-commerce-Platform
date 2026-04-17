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
import org.springframework.cache.annotation.Cacheable;
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
            products = productRepository.findByStatusNotOrderByCreatedAtDesc(ProductStatus.HIDDEN, pageable);
        }
        // type이 있으면 해당 타입 상품만 조회
        else {
            products = productRepository.findByTypeAndStatusNotOrderByCreatedAtDesc(type, ProductStatus.HIDDEN, pageable);
        }

        return products.map(ProductListResponse::from); // 응답 DTO로 변환
    }

    // 상품 상세 조회
    public ProductDetailResponse getProduct(Long productId) {
        // Service 계층에서 직접 예외 처리 로직을 작성
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 비공개 상품은 상세 조회 불가
        if (product.getStatus() == ProductStatus.HIDDEN) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return ProductDetailResponse.from(product);
    }

    // 검색 v1
    public Page<ProductListResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository
                .findByNameContainingAndStatusNotOrderByCreatedAtDesc(
                        keyword,
                        ProductStatus.HIDDEN,
                        pageable
                )
                .map(ProductListResponse::from);
    }

    // 검색 v2 (캐시 적용)
    @Cacheable(
            value = "productSearch",
            key = "#keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public Page<ProductListResponse> searchProductsWithCache(String keyword, Pageable pageable) {
        return productRepository
                .findByNameContainingAndStatusNotOrderByCreatedAtDesc(
                        keyword,
                        ProductStatus.HIDDEN,
                        pageable
                )
                .map(ProductListResponse::from);
    }
}
