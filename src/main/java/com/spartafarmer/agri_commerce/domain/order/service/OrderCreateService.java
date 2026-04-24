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
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.entity.OrderItem;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
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
public class OrderCreateService {
    // 최소 주문 금액
    private static final long MIN_ORDER_AMOUNT = 20_000L;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserCouponRepository userCouponRepository;

    /**
     * 주문 생성 전체 흐름
     * - 사용자/장바구니 조회
     * - 상품 검증 및 총액 계산
     * - 쿠폰 검증 및 할인 적용
     * - 최소 주문 금액 검증
     * - 주문 생성 및 재고 차감
     * - 쿠폰 사용 처리 및 장바구니 비우기
    */


    public OrderCreateResponse createOrder(Long userId, Long userCouponId) {
        User user = getUser(userId);
        Cart cart = getCart(user);
        List<CartItem> cartItems = getCartItems(cart);

        boolean hasCoupon = userCouponId != null;
        long totalPrice = calculateTotalPrice(cartItems, hasCoupon);

        UserCoupon userCoupon = getValidatedUserCoupon(userCouponId, userId);
        long discountPrice = getDiscountPrice(userCoupon);
        long finalPrice = calculateFinalPrice(totalPrice, discountPrice);

        Order order = Order.create(user, userCoupon, totalPrice, discountPrice, finalPrice);
        createOrderItemsAndDecreaseStock(order, cartItems);

        orderRepository.save(order);

        useCouponIfPresent(userCoupon);
        cart.clearCartItems();

        return toResponse(order);
    }

    // 사용자 조회
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 장바구니 조회
    private Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }

    // 장바구니 아이템 조회 및 비어있는지 검증
    private List<CartItem> getCartItems(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }
        return cartItems;
    }

    // 상품 검증 및 총 주문 금액 계산
    private long calculateTotalPrice(List<CartItem> cartItems, boolean hasCoupon) {
        long totalPrice = 0L;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            // 상품 주문 가능 여부 검증 (재고, 상태)
            product.validateOrderable(item.getQuantity());
            // 특가 상품 포함 시 쿠폰 사용 불가
            if (hasCoupon && product.getSpecialPrice() != null) {
                throw new CustomException(ErrorCode.COUPON_NOT_APPLICABLE);
            }

            totalPrice += product.getSalePrice() * item.getQuantity();
        }

        return totalPrice;
    }

    // 쿠폰 조회 및 사용 가능 여부 검증
    private UserCoupon getValidatedUserCoupon(Long userCouponId, Long userId) {
        if (userCouponId == null) {
            return null;
        }

        UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(userCouponId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        userCoupon.validateUsable(LocalDateTime.now());
        return userCoupon;
    }

    // 할인 금액 계산
    private long getDiscountPrice(UserCoupon userCoupon) {
        if (userCoupon == null) {
            return 0L;
        }
        return userCoupon.getCoupon().getDiscountAmount();
    }

    // 최종 결제 금액 계산 및 최소 주문 금액 검증
    private long calculateFinalPrice(long totalPrice, long discountPrice) {
        long finalPrice = totalPrice - discountPrice;

        if (finalPrice < MIN_ORDER_AMOUNT) {
            throw new CustomException(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET);
        }

        return finalPrice;
    }

    // 주문 상품 생성 및 재고 차감
    private void createOrderItemsAndDecreaseStock(Order order, List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
                Product product = item.getProduct();
                // 재고 차감
                product.decreaseStock(item.getQuantity());
                // 주문 상품 생성
                OrderItem.create(
                        order,
                        product,
                        product.getSalePrice(),
                        item.getQuantity()
                );
        }
    }

    // 쿠폰 사용 처리
    private void useCouponIfPresent(UserCoupon userCoupon) {
        if (userCoupon != null) {
            userCoupon.use();
        }
    }

    // 주문 응답 DTO 변환
    private OrderCreateResponse toResponse(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getTotalPrice(),
                order.getDiscountPrice(),
                order.getFinalPrice(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}