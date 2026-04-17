package com.spartafarmer.agri_commerce.domain.order.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderItemResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.entity.OrderItem;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserCouponRepository userCouponRepository;

    // 장바구니 전체 주문 생성
    public OrderCreateResponse createOrder(Long userId, Long userCouponId) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }

        long totalPrice = 0L;
        boolean hasCoupon = (userCouponId != null);

        // 상품 검증 + 금액 계산
        for (CartItem item : cartItems) {

            Product product = item.getProduct();

            product.validateOrderable(item.getQuantity());

            // 특가 + 쿠폰 제한
            if (hasCoupon && product.getSpecialPrice() != null) {
                throw new CustomException(ErrorCode.COUPON_NOT_APPLICABLE);
            }

            totalPrice += product.getSalePrice() * item.getQuantity();
        }

        // 쿠폰 처리
        UserCoupon userCoupon = null;
        long discountPrice = 0L;

        if (hasCoupon) {

            userCoupon = userCouponRepository.findById(userCouponId)
                    .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

            userCoupon.validateUsable(LocalDateTime.now());

            discountPrice = userCoupon.getCoupon().getDiscountAmount();
        }

        long finalPrice = totalPrice - discountPrice;

        // 최소 주문 금액
        if (finalPrice < 20000) {
            throw new CustomException(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET);
        }

        //  주문 생성
        Order order = Order.create(user, userCoupon, totalPrice, discountPrice, finalPrice);

        // 주문 아이템 + 재고 차감
        for (CartItem item : cartItems) {

            Product product = item.getProduct();

            product.decreaseStock(item.getQuantity());

            OrderItem.create(
                    order,
                    product,
                    product.getSalePrice(),
                    item.getQuantity()
            );
        }

        orderRepository.save(order);

        // 쿠폰 사용 처리
        if (userCoupon != null) {
            userCoupon.use();
        }

        // 주문 완료 후 장바구니 비우기
        cart.clearCartItems();

        // 응답
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderItems().stream()
                        .map(i -> new OrderItemResponse(
                                i.getProduct().getName(),
                                i.getQuantity(),
                                i.getPrice(),
                                i.getPrice() * i.getQuantity()
                        ))
                        .toList(),
                order.getTotalPrice(),
                order.getDiscountPrice(),
                order.getFinalPrice(),
                order.getStatus().name(),
                order.getCreatedAt()
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