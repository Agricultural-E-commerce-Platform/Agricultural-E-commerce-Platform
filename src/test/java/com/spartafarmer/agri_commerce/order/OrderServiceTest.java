package com.spartafarmer.agri_commerce.order;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.lock.LockService;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.entity.OrderStatus;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.order.service.OrderCreateService;
import com.spartafarmer.agri_commerce.domain.order.service.OrderService;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LockService lockService;

    @Mock
    private OrderCreateService orderCreateService;

    @Test
    void 주문생성_성공() {
        // given
        User user = mock(User.class);
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.of(cart));
        given(cart.getCartItems()).willReturn(List.of(cartItem));
        given(cartItem.getProduct()).willReturn(product);
        given(product.getId()).willReturn(1L);

        OrderCreateResponse expected = new OrderCreateResponse(
                1L, List.of(), 25000L, 0L, 25000L, "COMPLETED", LocalDateTime.now()
        );
        given(lockService.executeWithLocks(anyList(), any(), any())).willReturn(expected);

        // when
        OrderCreateResponse result = orderService.createOrder(1L, null);

        // then
        assertThat(result.finalPrice()).isEqualTo(25000L);
        assertThat(result.status()).isEqualTo("COMPLETED");
    }

    @Test
    void 주문생성_실패_유저없음() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void 주문생성_실패_장바구니없음() {
        // given
        User user = mock(User.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CART_NOT_FOUND));
    }

    @Test
    void 주문목록조회_성공() {
        // given
        User user = mock(User.class);
        Order order = mock(Order.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(orderRepository.findAllByUserIdOrderByCreatedAtDesc(anyLong())).willReturn(List.of(order));
        given(order.getId()).willReturn(1L);
        given(order.getOrderItems()).willReturn(List.of());
        given(order.getFinalPrice()).willReturn(25000L);
        given(order.getStatus()).willReturn(OrderStatus.COMPLETED);
        given(order.getCreatedAt()).willReturn(LocalDateTime.now());

        // when
        List<OrderListResponse> result = orderService.getOrders(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).finalPrice()).isEqualTo(25000L);
        assertThat(result.get(0).status()).isEqualTo("COMPLETED");
    }

    @Test
    void 주문목록조회_실패_유저없음() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrders(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
