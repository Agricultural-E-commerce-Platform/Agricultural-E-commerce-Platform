package com.spartafarmer.agri_commerce.order;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.order.service.OrderCreateService;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest {

    @InjectMocks
    private OrderCreateService orderCreateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Test
    void 주문생성_성공() {
        User user = 유저();
        Product product1 = 상품("사과", 15000L, 12000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품("배", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = Cart.create(user);
        CartItem.create(cart, product1, product1.getSalePrice(), 1);
        CartItem.create(cart, product2, product2.getSalePrice(), 1);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderCreateResponse result = orderCreateService.createOrder(1L, null);

        assertThat(result.originalPrice()).isEqualTo(22000L);
        assertThat(result.discountAmount()).isEqualTo(0L);
        assertThat(result.finalPrice()).isEqualTo(22000L);
        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(cart.getCartItems()).isEmpty();
        assertThat(product1.getStock()).isEqualTo(9);
        assertThat(product2.getStock()).isEqualTo(9);
    }

    @Test
    void 주문생성_실패_장바구니비어있음() {
        User user = 유저();
        Cart cart = Cart.create(user);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderCreateService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CART_EMPTY));
    }

    @Test
    void 주문생성_실패_최소주문금액미만() {
        User user = 유저();
        Product product = 상품("양파", 10000L, 8000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = Cart.create(user);
        CartItem.create(cart, product, product.getSalePrice(), 1);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderCreateService.createOrder(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET));
    }

    @Test
    void 주문생성_실패_특가상품포함_쿠폰사용불가() {
        User user = 유저();
        Product specialProduct = 상품("특가 딸기", 25000L, 20000L, 10, ProductStatus.ON_SALE, 12000L);

        Cart cart = Cart.create(user);
        CartItem.create(cart, specialProduct, specialProduct.getSalePrice(), 1);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderCreateService.createOrder(1L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COUPON_NOT_APPLICABLE));
    }

    @Test
    void 주문생성_성공_쿠폰적용() {
        User user = 유저();
        Product product1 = 상품("감자", 18000L, 15000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품("고구마", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = Cart.create(user);
        CartItem.create(cart, product1, product1.getSalePrice(), 1);
        CartItem.create(cart, product2, product2.getSalePrice(), 1);

        Coupon coupon = Coupon.create(
                "5천원 할인 쿠폰",
                5000L,
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        UserCoupon userCoupon = UserCoupon.issue(user, coupon, LocalDateTime.now());

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));
        given(userCouponRepository.findByIdAndUserId(anyLong(), anyLong())).willReturn(Optional.of(userCoupon));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderCreateResponse result = orderCreateService.createOrder(1L, 1L);

        assertThat(result.originalPrice()).isEqualTo(25000L);
        assertThat(result.discountAmount()).isEqualTo(5000L);
        assertThat(result.finalPrice()).isEqualTo(20000L);
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.USED);
        assertThat(cart.getCartItems()).isEmpty();
    }

    private User 유저() {
        return User.create(
                "test@test.com",
                "Password123",
                "테스트유저",
                User.formatPhone("01012345678"),
                "서울",
                UserRole.USER
        );
    }

    private Product 상품(String name, Long normalPrice, Long salePrice, int stock,
                       ProductStatus status, Long specialPrice) {
        return Product.create(
                name,
                specialPrice == null ? ProductType.NORMAL : ProductType.SPECIAL,
                normalPrice,
                salePrice,
                specialPrice,
                stock,
                status,
                name + ".jpg"
        );
    }
}