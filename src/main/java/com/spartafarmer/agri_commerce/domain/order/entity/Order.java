package com.spartafarmer.agri_commerce.domain.order.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 쿠폰 미적용시 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon coupon;

    // 총 상품 금액(할인 전)
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    // 쿠폰 미적용 시 0
    @Column(name = "discount_price", nullable = false)
    private Long discountPrice;

    // 최종 결제 금액(할인가격 포함)
    @Column(name = "final_price", nullable = false)
    private Long finalPrice;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // 주문 상품 목록
    // Order 저장하면 OrderItem도 같이 저장됨
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문 생성 (성공한 경우만 생성됨)
    // 실패 - DB 저장 안됨, 성공시 COMPLETED만 존재
    public static Order create(User user, UserCoupon coupon,
                               Long totalPrice, Long discountPrice, Long finalPrice) {

        Order order = new Order();
        order.user = user;
        order.coupon = coupon;
        order.totalPrice = totalPrice;
        order.discountPrice = discountPrice;
        order.finalPrice = finalPrice;
        order.status = OrderStatus.COMPLETED;
        return order;
    }

    // 주문에 상품을 포함시키기 위한 메서드 (양방향 관계 동기화)
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
