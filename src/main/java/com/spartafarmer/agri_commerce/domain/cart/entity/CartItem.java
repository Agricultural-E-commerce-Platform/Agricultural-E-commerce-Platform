package com.spartafarmer.agri_commerce.domain.cart.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private int quantity;

    private CartItem(Cart cart, Product product, Long price, int quantity) {
        this.cart = cart;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
    }

    // 장바구니 상품 생성 및 장바구니에 추가
    public static CartItem create(Cart cart, Product product, Long price, int quantity) {
        CartItem item = new CartItem(cart, product, price, quantity);
        cart.addCartItem(item);
        return item;
    }

    // 수량 변경
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}