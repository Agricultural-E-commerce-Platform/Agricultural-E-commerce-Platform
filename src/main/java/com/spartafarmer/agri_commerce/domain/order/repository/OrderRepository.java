package com.spartafarmer.agri_commerce.domain.order.repository;

import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 사용자 주문 목록 (최신순)
    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
