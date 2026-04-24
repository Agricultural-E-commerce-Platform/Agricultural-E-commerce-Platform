package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 상품 목록 조회 + 특가 상품 조회
    @GetMapping
    public Page<ProductListResponse> getProducts(
            @RequestParam(required = false) ProductType type,
            @PageableDefault(size = 10)
            @Max(100) Pageable pageable
    ) {
        return productService.getProducts(type, pageable);
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

}
