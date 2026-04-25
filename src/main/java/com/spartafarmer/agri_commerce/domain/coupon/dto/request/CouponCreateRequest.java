package com.spartafarmer.agri_commerce.domain.coupon.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponCreateRequest(

        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        String name,

        @NotNull(message = "할인 금액은 필수입니다.")
        @Min(value = 1, message = "할인 금액은 1원 이상이어야 합니다.")
        Long discountAmount,

        @NotNull(message = "발급 수량은 필수입니다.")
        @Min(value = 1, message = "발급 수량은 1개 이상이어야 합니다.")
        Integer totalQuantity,

        @NotNull(message = "시작 시각은 필수입니다.")
        LocalDateTime startTime,

        @NotNull(message = "종료 시각은 필수입니다.")
        LocalDateTime endTime
) {}
