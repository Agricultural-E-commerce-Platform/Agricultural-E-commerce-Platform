package com.spartafarmer.agri_commerce.domain.product.dto;

public record PopularKeywordResponse(
        int rank,
        String keyword,
        long searchCount
) {}
