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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductSearchController {

    private final ProductService productService;

    @GetMapping("/search")
    public Page<ProductListResponse> searchProducts(
            @RequestParam @Size(min = 1, max = 50) String keyword, // 검색어 길이 제한
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (pageable.getPageSize() > 100) {
            throw new CustomException(ErrorCode.INVALID_REQUEST); // 과도한 페이지 크기 방지
        }

        return productService.searchProducts(keyword, pageable); // 검색 서비스 호출
    }
}