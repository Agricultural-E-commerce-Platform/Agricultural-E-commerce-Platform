package com.spartafarmer.agri_commerce.order;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.lock.LockService;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.entity.OrderItem;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.order.service.OrderCreateService;
import com.spartafarmer.agri_commerce.domain.order.service.OrderService;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
        User user = User.create(
                "test@test.com", "pass1234", "테스트유저",
                "010-1234-5678", "서울", UserRole.USER
        );

        Product product = Product.create(
                "사과", ProductType.NORMAL,
                15000L, 12000L, null,
                10, ProductStatus.ON_SALE, null
        );

        Cart cart = Cart.create(user);
        CartItem.create(cart, product, product.getSalePrice(), 2);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.of(cart));

        OrderCreateResponse expected = new OrderCreateResponse(
                1L, List.of(), 24000L, 0L, 24000L, "COMPLETED", LocalDateTime.now()
        );
        given(lockService.executeWithLocks(anyList(), any(), any())).willReturn(expected);

        // when
        OrderCreateResponse result = orderService.createOrder(1L, null);

        // then
        assertThat(result.finalPrice()).isEqualTo(24000L);
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
        User user = User.create(
                "test@test.com", "pass1234", "테스트유저",
                "010-1234-5678", "서울", UserRole.USER
        );

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CART_NOT_FOUND));
    }

    @Test
    void 주문생성_실패_최소주문금액미만() {
        // given
        User user = User.create(
                "test@test.com", "pass1234", "테스트유저",
                "010-1234-5678", "서울", UserRole.USER
        );

        Product product = Product.create(
                "사과", ProductType.NORMAL,
                10000L, 8000L, null,
                10, ProductStatus.ON_SALE, null
        );

        Cart cart = Cart.create(user);
        CartItem.create(cart, product, product.getSalePrice(), 1);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.of(cart));
        given(lockService.executeWithLocks(anyList(), any(), any()))
                .willThrow(new CustomException(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET));
    }

    @Test
    void 주문목록조회_성공() {
        // given
        User user = User.create(
                "test@test.com", "pass1234", "테스트유저",
                "010-1234-5678", "서울", UserRole.USER
        );

        Product product = Product.create(
                "사과", ProductType.NORMAL,
                15000L, 12000L, null,
                10, ProductStatus.ON_SALE, null
        );

        Order order = Order.create(user, null, 25000L, 0L, 25000L);
        OrderItem.create(order, product, product.getSalePrice(), 2);

        given(orderRepository.findAllWithItemsAndProductByUserId(anyLong())).willReturn(List.of(order));

        // when
        List<OrderListResponse> result = orderService.getOrders(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).finalPrice()).isEqualTo(25000L);
        assertThat(result.get(0).status()).isEqualTo("COMPLETED");
        assertThat(result.get(0).orderItems()).hasSize(1);
        assertThat(result.get(0).orderItems().get(0).productName()).isEqualTo("사과");
    }

    @Test
    void 주문생성_락키생성_중복제거_정렬성공() {
        User user = User.create(
                "test@test.com", "pass1234", "테스트유저",
                "010-1234-5678", "서울", UserRole.USER
        );

        Product product1 = Product.create(
                "사과", ProductType.NORMAL, 15000L, 12000L, null,
                10, ProductStatus.ON_SALE, "apple.jpg"
        );
        Product product2 = Product.create(
                "배", ProductType.NORMAL, 12000L, 10000L, null,
                10, ProductStatus.ON_SALE, "pear.jpg"
        );

        // id 세팅이 필요하면 실제 엔티티 구조에 맞게 생성/리플렉션/테스트 헬퍼 사용
        // 여기서는 설명상 product1 id=2, product2 id=1 이라고 가정
        setProductId(product1, 2L);
        setProductId(product2, 1L);

        Cart cart = Cart.create(user);
        CartItem.create(cart, product1, product1.getSalePrice(), 1);
        CartItem.create(cart, product2, product2.getSalePrice(), 1);
        CartItem.create(cart, product1, product1.getSalePrice(), 2); // 중복 상품

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.of(cart));

        OrderCreateResponse expected = new OrderCreateResponse(
                1L, List.of(), 34000L, 0L, 34000L, "COMPLETED", LocalDateTime.now()
        );
        given(lockService.executeWithLocks(any(), any(), any())).willReturn(expected);

        orderService.createOrder(1L, null);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(lockService).executeWithLocks(keysCaptor.capture(), ttlCaptor.capture(), any());

        assertThat(keysCaptor.getValue())
                .containsExactly("product:stock:1", "product:stock:2");
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofSeconds(3));
    }

    private void setProductId(Product product, Long id) {
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
