package com.spartafarmer.agri_commerce.domain.order.repository;

import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 사용자 주문 목록 조회 (주문상품, 상품 함께 조회)
    @Query("""
        select distinct o
        from Order o
        join fetch o.orderItems oi
        join fetch oi.product
        where o.user.id = :userId
        order by o.createdAt desc
    """)

    // 사용자 주문 목록 (최신순)
    List<Order> findAllWithItemsAndProductByUserId(@Param("userId") Long userId);
}
