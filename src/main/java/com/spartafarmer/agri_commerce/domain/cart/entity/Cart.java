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
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
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

    // 장바구니 상품 추가
    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }
}