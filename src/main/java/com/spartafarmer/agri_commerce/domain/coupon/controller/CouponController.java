package com.spartafarmer.agri_commerce.domain.coupon.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponIssueResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponListResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.UserCouponResponse;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 쿠폰 전체 목록 조회 (관리자)
    @Secured("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponListResponse>>> getCoupons(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons(pageable)));
    }

    // 내 쿠폰 목록 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyCoupons(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getMyCoupons(authUser.getId())));
    }

    // 선착순 쿠폰 발급
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
               .body(ApiResponse.success(201, "쿠폰이 발급되었습니다.", couponService.issueCoupon(couponId, authUser.getId())));
        }
}
