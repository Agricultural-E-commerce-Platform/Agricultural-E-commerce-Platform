package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.PopularKeywordResponse;
import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/search")
public class PopularSearchController {

    private final PopularSearchService popularSearchService;

    // 인기검색어 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> getPopularKeywords() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "인기 검색어 조회 성공",
                        popularSearchService.getTopKeywords()
                ));
    }
}