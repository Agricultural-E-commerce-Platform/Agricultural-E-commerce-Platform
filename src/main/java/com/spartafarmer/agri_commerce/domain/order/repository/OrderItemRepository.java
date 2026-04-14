package com.spartafarmer.agri_commerce.domain.order.repository;

import com.spartafarmer.agri_commerce.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 주문의 주문 상품 목록 조회
    List<OrderItem> findAllByOrderId(Long orderId);
}