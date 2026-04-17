package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
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
            @RequestParam @Size(min = 1, max = 50) String keyword, // 검색어 제한
            @PageableDefault(size = 10) Pageable pageable
    ) {
        String normalizedKeyword = keyword.trim(); // 공백 제거

        // 공백만 입력한 경우 예외
        if (normalizedKeyword.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // v2에서만 검색어 집계
        popularSearchService.increaseKeyword(normalizedKeyword);

        // 검색 실행
        return productService.searchProductsWithCache(normalizedKeyword, pageable);
    }
}
