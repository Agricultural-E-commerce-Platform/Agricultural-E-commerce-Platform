package com.spartafarmer.agri_commerce.domain.product.controller;

import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/search")
public class PopularSearchController {

    private final PopularSearchService popularSearchService;

    // 인기검색어 조회
    @GetMapping("/popular")
    public List<Map<String, Object>> getPopularKeywords() {
        return popularSearchService.getTopKeywords();
    }
}
