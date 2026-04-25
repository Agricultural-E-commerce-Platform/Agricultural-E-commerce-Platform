package com.spartafarmer.agri_commerce.domain.cart.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.cart.dto.*;
import com.spartafarmer.agri_commerce.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    // 장바구니 담기
    @PostMapping
    public ResponseEntity<ApiResponse<CartAddResponse>> addCart(
            @Valid @RequestBody CartAddRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "장바구니 담기 성공",
                        cartService.addCart(authUser.getId(), request)));
    }

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(200, "장바구니 조회 성공",
                        cartService.getCart(authUser.getId())));
    }

    // 수량 변경
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartUpdateResponse>> updateQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartUpdateRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(200, "장바구니 수량 변경 성공",
                        cartService.updateQuantity(cartItemId, request, authUser.getId())));
    }

    // 장바구니 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        cartService.deleteCartItem(cartItemId, authUser.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(200, "장바구니 상품 삭제 성공", null));
    }
}