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

    // 장바구니 전체 주문 생성 (락)
    public OrderCreateResponse createOrder(Long userId, Long userCouponId) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 장바구니 조회
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        List<String> keys = cart.getCartItems().stream()
                .map(item -> item.getProduct().getId())
                .distinct().sorted().map(productId -> "product:stock:" + productId)
                .toList();

        return lockService.executeWithLocks(
                keys,
                Duration.ofSeconds(3),
                () -> orderCreateService.createOrder(userId, userCouponId)
        );
    }

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrders(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(order -> new OrderListResponse(
                        order.getId(),
                        order.getOrderItems().stream()
                                .map(i -> new OrderItemResponse(
                                        i.getProduct().getName(),
                                        i.getQuantity(),
                                        i.getPrice(),
                                        i.getPrice() * i.getQuantity()
                                ))
                                .toList(),
                        order.getFinalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                ))
                .toList();
    }
}