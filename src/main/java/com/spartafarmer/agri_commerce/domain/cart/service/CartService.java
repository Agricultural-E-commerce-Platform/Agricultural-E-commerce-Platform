package com.spartafarmer.agri_commerce.domain.cart.service;

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

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.validateOrderable(request.getQuantity());

        // 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.create(user)));

        // 기존 상품 여부 확인
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

        // 신규 생성
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
        List<CartItemResponse> items = cart.getCartItems().stream()
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
        CartItem cartItem = cartItemRepository.findByIdAndCart_User_Id(cartItemId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));


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

    // 장바구니 삭제
    @Transactional
    public void deleteCartItem(Long cartItemId, Long userId) {

        // 삭제 대상 조회
        CartItem cartItem = cartItemRepository.findByIdAndCart_User_Id(cartItemId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Cart가 CartItem 생명주기를 관리하도록 부모 컬렉션에서 제거
        cartItem.getCart().removeCartItem(cartItem);
    }
}