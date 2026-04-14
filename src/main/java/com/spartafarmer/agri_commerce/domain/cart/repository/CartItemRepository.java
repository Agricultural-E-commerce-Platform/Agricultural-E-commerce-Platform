package com.spartafarmer.agri_commerce.domain.cart.repository;

import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니의 삭제되지 않은 상품 목록 조회
    List<CartItem> findByCartAndDeletedAtIsNull(Cart cart);

    // 특정 상품이 장바구니에 이미 있는지 확인 (삭제되지 않은 것만)
    Optional<CartItem> findByCartAndProductIdAndDeletedAtIsNull(Cart cart, Long productId);
}