package com.spartafarmer.agri_commerce.domain.coupon.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    // 쿠폰 생성 (관리자)
    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponCreateResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "쿠폰이 생성되었습니다.", couponService.createCoupon(request)));
    }
}
