package com.spartafarmer.agri_commerce.domain.cart.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.cart.dto.*;
import com.spartafarmer.agri_commerce.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    // 장바구나 담기
    @PostMapping
    public ApiResponse<CartAddResponse> addCart(
            @Valid @RequestBody CartAddRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.success(201, "장바구니 담기 성공", cartService.addCart(authUser.getId(), request));
    }

    // 장바구니 조회
    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(200, "장바구니 조회 성공", cartService.getCart(authUser.getId()));
    }

    // 수량 변경
    @PatchMapping("/{cartItemId}")
    public ApiResponse<CartUpdateResponse> updateQuantity(@PathVariable Long cartItemId,
                                                          @Valid @RequestBody CartUpdateRequest request,
                                                          @AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(200, "장바구니 수량 변경 성공", cartService.updateQuantity(cartItemId, request, authUser.getId()));
    }

    // 장바구니 삭제 (소프트 딜리트)
    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<Void> deleteCartItem(@PathVariable Long cartItemId, @AuthenticationPrincipal AuthUser authUser) {
        cartService.deleteCartItem(cartItemId, authUser.getId());
        return ApiResponse.success(200, "장바구니 상품 삭제 성공", null);
    }

}