package com.spartafarmer.agri_commerce.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartItemRepository;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateRequest;
import com.spartafarmer.agri_commerce.domain.order.entity.Order;
import com.spartafarmer.agri_commerce.domain.order.entity.OrderStatus;
import com.spartafarmer.agri_commerce.domain.order.repository.OrderRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired UserCouponRepository userCouponRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired EntityManager entityManager;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.create(
                        "order_test@test.com",
                        "Password123",
                        "주문유저",
                        User.formatPhone("01012345678"),
                        "서울시 강남구",
                        UserRole.USER
                )
        );

        accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    void 주문생성_성공() throws Exception {
        // given
        Product product1 = 상품저장("사과", 15000L, 12000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("배", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("주문 생성 성공"))
                .andExpect(jsonPath("$.data.originalPrice").value(22000))
                .andExpect(jsonPath("$.data.discountAmount").value(0))
                .andExpect(jsonPath("$.data.finalPrice").value(22000))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.orderItems.length()").value(2));

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.COMPLETED);

        Product savedProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        Product savedProduct2 = productRepository.findById(product2.getId()).orElseThrow();
        assertThat(savedProduct1.getStock()).isEqualTo(9);
        assertThat(savedProduct2.getStock()).isEqualTo(9);

        entityManager.flush();
        entityManager.clear();

        Cart savedCart = cartRepository.findByUser(user).orElseThrow();
        assertThat(savedCart.getCartItems()).isEmpty();
    }

    @Test
    void 주문생성_성공_쿠폰적용() throws Exception {
        // given
        Product product1 = 상품저장("감자", 18000L, 15000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("고구마", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        Coupon coupon = couponRepository.save(
                Coupon.create(
                        "5천원 할인 쿠폰",
                        5000L,
                        100,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)
                )
        );

        UserCoupon userCoupon = userCouponRepository.save(
                UserCoupon.issue(user, coupon, LocalDateTime.now())
        );

        // when
        ResultActions result = 주문요청(userCoupon.getId());

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("주문 생성 성공"))
                .andExpect(jsonPath("$.data.originalPrice").value(25000))
                .andExpect(jsonPath("$.data.discountAmount").value(5000))
                .andExpect(jsonPath("$.data.finalPrice").value(20000))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        UserCoupon savedUserCoupon = userCouponRepository.findById(userCoupon.getId()).orElseThrow();
        assertThat(savedUserCoupon.getStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    void 주문생성_실패_장바구니없음() throws Exception {
        // given

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("장바구니를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_최소주문금액미만() throws Exception {
        // given
        Product product = 상품저장("양파", 10000L, 8000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product, 1);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("최소 주문 금액은 20,000원 이상입니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void 주문생성_실패_재고부족() throws Exception {
        // given
        Product product = 상품저장("토마토", 15000L, 12000L, 1, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product, 2);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("재고가 부족합니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void 주문생성_실패_품절상품() throws Exception {
        // given
        Product product = 상품저장("품절 상품", 25000L, 22000L, 0, ProductStatus.SOLD_OUT, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product, 1);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("품절된 상품입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_판매종료상품() throws Exception {
        // given
        Product product = 상품저장("판매종료 상품", 25000L, 22000L, 10, ProductStatus.SALE_ENDED, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product, 1);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("판매 종료된 상품입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_판매전상품() throws Exception {
        // given
        Product product = 상품저장("판매전 상품", 25000L, 22000L, 10, ProductStatus.READY, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product, 1);

        // when
        ResultActions result = 주문요청(null);

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("현재 판매 중인 상품이 아닙니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_쿠폰없음() throws Exception {
        // given
        Product product1 = 상품저장("사과", 18000L, 15000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("배", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        // when
        ResultActions result = 주문요청(9999L);

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("쿠폰을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_이미사용한쿠폰() throws Exception {
        // given
        Product product1 = 상품저장("감자", 18000L, 15000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("고구마", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        Coupon coupon = couponRepository.save(
                Coupon.create(
                        "5천원 할인 쿠폰",
                        5000L,
                        100,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)
                )
        );

        UserCoupon userCoupon = userCouponRepository.save(
                UserCoupon.issue(user, coupon, LocalDateTime.now())
        );
        userCoupon.use();

        // when
        ResultActions result = 주문요청(userCoupon.getId());

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용된 쿠폰입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_만료된쿠폰() throws Exception {
        // given
        Product product1 = 상품저장("감자", 18000L, 15000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("고구마", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        Coupon coupon = couponRepository.save(
                Coupon.create(
                        "5천원 할인 쿠폰",
                        5000L,
                        100,
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().minusDays(5)
                )
        );

        UserCoupon expiredUserCoupon = userCouponRepository.save(
                UserCoupon.issue(user, coupon, LocalDateTime.now().minusDays(10))
        );

        // when
        ResultActions result = 주문요청(expiredUserCoupon.getId());

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("만료된 쿠폰입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문생성_실패_특가상품포함으로쿠폰사용불가() throws Exception {
        // given
        Product specialProduct = 상품저장("특가 딸기", 25000L, 20000L, 10, ProductStatus.ON_SALE, 12000L);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, specialProduct, 1);

        Coupon coupon = couponRepository.save(
                Coupon.create(
                        "5천원 할인 쿠폰",
                        5000L,
                        100,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)
                )
        );

        UserCoupon userCoupon = userCouponRepository.save(
                UserCoupon.issue(user, coupon, LocalDateTime.now())
        );

        // when
        ResultActions result = 주문요청(userCoupon.getId());

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("특가 상품이 포함된 주문에는 쿠폰을 사용할 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 주문목록조회_성공() throws Exception {
        // given
        Product product1 = 상품저장("사과", 15000L, 12000L, 10, ProductStatus.ON_SALE, null);
        Product product2 = 상품저장("배", 12000L, 10000L, 10, ProductStatus.ON_SALE, null);

        Cart cart = 장바구니생성(user);
        장바구니상품추가(cart, product1, 1);
        장바구니상품추가(cart, product2, 1);

        주문요청(null).andExpect(status().isCreated());

        // when
        ResultActions result = mockMvc.perform(get("/api/orders")
                .header("Authorization", bearerToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("주문 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].finalPrice").value(22000))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[0].orderItems.length()").value(2));
    }

    private ResultActions 주문요청(Long userCouponId) throws Exception {
        return mockMvc.perform(post("/api/orders")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderCreateRequest(userCouponId))));
    }

    private Cart 장바구니생성(User user) {
        return cartRepository.save(Cart.create(user));
    }

    private void 장바구니상품추가(Cart cart, Product product, int quantity) {
        cartItemRepository.save(
                CartItem.create(cart, product, product.getSalePrice(), quantity)
        );
    }

    private Product 상품저장(String name, Long normalPrice, Long salePrice, int stock,
                         ProductStatus status, Long specialPrice) {
        return productRepository.save(
                Product.create(
                        name,
                        specialPrice == null ? ProductType.NORMAL : ProductType.SPECIAL,
                        normalPrice,
                        salePrice,
                        specialPrice,
                        stock,
                        status,
                        name + ".jpg"
                )
        );
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }
}