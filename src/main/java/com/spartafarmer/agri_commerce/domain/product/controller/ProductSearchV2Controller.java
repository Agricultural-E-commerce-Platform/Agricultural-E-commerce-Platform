package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
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
    private final PopularSearchService popularSearchService;

    // 검색 v2 (캐시 적용 + 인기검색어 집계)
    @GetMapping("/search")
    public Page<ProductListResponse> searchProductsV2(
            @RequestParam @Size(min = 1, max = 50) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        String normalizedKeyword = productService.normalizeKeyword(keyword);

        // TODO: 실제 로그인 사용자 ID 꺼내는 방식으로 교체
        Long userId = 1L;

        // 회원당 동일 검색어 1회만 집계
        popularSearchService.increaseKeyword(userId, normalizedKeyword);

        return productService.searchProductsWithCache(normalizedKeyword, pageable);
    }
}
