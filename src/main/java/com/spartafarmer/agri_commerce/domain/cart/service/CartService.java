package com.spartafarmer.agri_commerce.domain.cart.service;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.cart.dto.*;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartItemRepository;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final long MIN_ORDER_AMOUNT = 20000L;

    // 장바구니 담기
    @Transactional
    public CartAddResponse addCart(Long userId, CartAddRequest request) {

        // 1. 수량 검증
        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 2. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        validateProductStatus(product);

        // 4. 재고 검증
        if (request.getQuantity() > product.getStock()) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        // 5. 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.create(user)));

        // 6. 기존 상품 여부 확인
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK);
            }

            cartItem.updateQuantity(newQuantity);

            return new CartAddResponse(
                    cartItem.getId(),
                    product.getId(),
                    product.getName(),
                    cartItem.getPrice(),
                    cartItem.getQuantity()
            );
        }

        // 7. 신규 생성
        CartItem newItem = CartItem.create(cart, product, product.getSalePrice(), request.getQuantity());
        cartItemRepository.save(newItem);

        return new CartAddResponse(
                newItem.getId(),
                product.getId(),
                product.getName(),
                newItem.getPrice(),
                newItem.getQuantity()
        );
    }

    // 장바구니 조회
    public CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
        List< CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getProduct().getStatus()
                )).toList();
        long totalPrice = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
        boolean isMinOrderAmountMet = totalPrice >= MIN_ORDER_AMOUNT;
        return new CartResponse(items, totalPrice, MIN_ORDER_AMOUNT, isMinOrderAmountMet);
    }

    // 수량 변경
    @Transactional
    public CartUpdateResponse updateQuantity(Long cartItemId, CartUpdateRequest request, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 내 장바구니인지 검증
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 수량 검증
        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 재고 초과 검증
        if (request.getQuantity() > cartItem.getProduct().getStock()) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        cartItem.updateQuantity(request.getQuantity());
        return new CartUpdateResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getPrice(),
                cartItem.getQuantity()
    );
    }

    // 장바구니 삭제(소프트 딜리트)
    @Transactional
    public void deleteCartItem(Long cartItemId, Long userId) {

        // 삭제 대상 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // Soft Delete 실행 (JPA가 update로 변환)
        cartItemRepository.delete(cartItem);
    }

    // 상품 상태 검증
    private void validateProductStatus(Product product) {
        if (product.getStatus() == ProductStatus.SOLD_OUT) {
            throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
        }
        if (product.getStatus() == ProductStatus.SALE_ENDED) {
            throw new CustomException(ErrorCode.PRODUCT_SALE_ENDED);
        }
    }
}
