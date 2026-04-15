package com.spartafarmer.agri_commerce.domain.order.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer quantity;

    private OrderItem(Product product, Long price, Integer quantity) {
        this.product = product;
        this.price = price;
        this.quantity = quantity;
    }

    // 주문상품 생성 후 해당 주문(Order)에 자동으로 포함시킨다.
    public static OrderItem create(Order order, Product product, Long price, Integer quantity) {
        OrderItem item = new OrderItem(product, price, quantity);
        order.addOrderItem(item);
        return item;
    }

    void setOrder(Order order) {
        this.order = order;
    }
}