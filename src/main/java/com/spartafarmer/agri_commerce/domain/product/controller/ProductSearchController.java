package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Validated
public class ProductSearchController {

    private final ProductService productService;

    // 검색 v1 (캐시 없음)
    @GetMapping("/search")
    public Page<ProductListResponse> searchProducts(
            @RequestParam @Size(min = 1, max = 50) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        String normalizedKeyword = productService.normalizeKeyword(keyword); // 공통 처리
        return productService.searchProducts(normalizedKeyword, pageable);
    }
}