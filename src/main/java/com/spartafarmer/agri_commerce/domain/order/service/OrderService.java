package com.spartafarmer.agri_commerce.domain.order.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.lock.LockService;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderItemResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final LockService lockService;
    private final OrderCreateService orderCreateService;

    private static final Duration STOCK_LOCK_TTL = Duration.ofSeconds(3);

    // 장바구니 전체 주문 생성 (락)
    public OrderCreateResponse createOrder(Long userId, Long userCouponId) {

        User user = getUser(userId);
        List<String> keys = getProductStockKeys(user);

        return lockService.executeWithLocks(
                keys,
                STOCK_LOCK_TTL,
                () -> orderCreateService.createOrder(userId, userCouponId)
        );
    }

    // 주문 목록 조회
    // 주문상품(OrderItem)과 상품(Product)을 함께 조회하여 N+1 문제 방지
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrders(Long userId) {

        List<Order> orders = orderRepository.findAllWithItemsAndProductByUserId(userId);

        return orders.stream()
                .map(order -> new OrderListResponse(
                        order.getId(),
                        order.getOrderItems().stream()
                                .map(OrderItemResponse::from)
                                .toList(),
                        order.getFinalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                ))
                .toList();
    }

    // 사용자 조회
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 장바구니 상품들의 재고 락 키 생성
    // - 중복 제거
    // - 정렬 후 고정된 순서로 락 획득
    private List<String> getProductStockKeys(User user) {
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        return cart.getCartItems().stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .map(productId -> "product:stock:" + productId)
                .toList();
    }
}