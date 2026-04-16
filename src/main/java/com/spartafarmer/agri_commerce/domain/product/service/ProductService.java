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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 목록 조회
    public Page<ProductListResponse> getProducts(ProductType type, Pageable pageable) {

        // 특가 상품 조회인 경우:
        // 현재 판매중(ON_SALE)인 특가 상품만 조회
        if (type == ProductType.SPECIAL) {
            return productRepository
                    .findByTypeAndStatusOrderByCreatedAtDesc(
                            ProductType.SPECIAL,   // 특가 상품만 조회
                            ProductStatus.ON_SALE, // 현재 판매중 상태만 조회
                            pageable
                    )
                    .map(ProductListResponse::from); // 엔티티를 DTO로 변환
        }

        Page<Product> products;

        // type이 없으면 전체 상품 목록 조회
        if (type == null) {
            products = productRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        // type이 있으면 해당 타입 상품만 조회
        else {
            products = productRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        }

        return products.map(ProductListResponse::from); // 응답 DTO로 변환
    }

    // 상품 상세 조회
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        // 특가 상품이고 아직 시작 전이면 상세 조회 불가
        if (product.getType() == ProductType.SPECIAL &&
                product.getStatus() == ProductStatus.READY) {
            throw new CustomException(ErrorCode.PRODUCT_SALE_NOT_STARTED); // 판매 시작 전 상품 예외
        }

        return ProductDetailResponse.from(product); // 판매 종료 상품은 상세 조회 가능
    }

    // 검색 API
    public Page<ProductListResponse> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST); // 빈 검색어 방지
        }

        return productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword, pageable)
                .map(ProductListResponse::from); // 검색 결과를 DTO로 변환해서 반환
    }
}
