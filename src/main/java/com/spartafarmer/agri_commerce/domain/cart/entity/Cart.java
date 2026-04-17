package com.spartafarmer.agri_commerce.domain.cart.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private Cart(User user) {
        this.user = user;
    }

    // 장바구니 생성
    public static Cart create(User user) {
        return new Cart(user);
    }

    // 장바구니 상품 목록 조회 (읽기 전용)
    public List<CartItem> getCartItems() {
        return Collections.unmodifiableList(cartItems);
    }

    // 장바구니 상품 추가(연관관계 편의 메서드)
    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }

    // 장바구니 상품 삭제(개별)
    // 유저가 장바구니 상품 삭제하는 것
    // orphanRemoval = true에 의해 DB에서도 해당 CartItem이 삭제됨
    public void removeCartItem(CartItem cartItem) {
        this.cartItems.remove(cartItem);
    }

    // 장바구니 비우기 - 전체 delete
    // 주문 완료 시 장바구니에 담긴 모든 상품을 제거하는 용도
    // orphanRemoval = true에 의해 모든 CartItem이 DB에서 삭제됨
    public void clearCartItems() {
        this.cartItems.clear();
    }
}