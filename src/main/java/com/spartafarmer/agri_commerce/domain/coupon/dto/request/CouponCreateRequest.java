package com.spartafarmer.agri_commerce.domain.coupon.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Long discountAmount;

    @NotNull
    @Min(1)
    private Integer totalQuantity;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
