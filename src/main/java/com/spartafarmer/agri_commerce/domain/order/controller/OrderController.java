package com.spartafarmer.agri_commerce.domain.order.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateRequest;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderCreateResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response =
                orderService.createOrder(authUser.getId(), request.getUserCouponId());

        return ApiResponse.success(
                201,
                "주문 생성 성공",
                response
        );
    }

    // 주문 목록 조회
    @GetMapping
    public ApiResponse<List<OrderListResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<OrderListResponse> response =
                orderService.getOrders(authUser.getId());

        return ApiResponse.success(
                200,
                "주문 목록 조회 성공",
                response
        );
    }
}