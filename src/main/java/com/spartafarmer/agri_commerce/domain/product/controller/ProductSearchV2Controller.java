package com.spartafarmer.agri_commerce.domain.product.controller;

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
@RequestMapping("/api/v2/products")
@Validated
public class ProductSearchV2Controller {

    private final ProductService productService;

    // 검색 v2 (캐시 적용)
    @GetMapping("/search")
    public Page<ProductListResponse> searchProductsV2(
            @RequestParam @Size(min = 1, max = 50) String keyword, // 검색어 제한
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return productService.searchProductsWithCache(keyword, pageable);
    }
}
