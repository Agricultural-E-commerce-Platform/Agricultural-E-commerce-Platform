package com.spartafarmer.agri_commerce.domain.coupon.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @NotBlank String name,
        @NotNull @Min(1) Long discountAmount,
        @NotNull @Min(1) Integer totalQuantity,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {}
