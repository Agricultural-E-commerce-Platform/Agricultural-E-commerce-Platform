package com.spartafarmer.agri_commerce.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartAddRequest;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartUpdateRequest;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartItemRepository;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.create(
                        "cart_test@test.com",
                        "Password123",
                        "장바구니유저",
                        User.formatPhone("01012345678"),
                        "서울시 강남구",
                        UserRole.USER
                )
        );

        accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    void 장바구니_상품추가_성공() throws Exception {
        // given
        Product product = 상품저장("사과", 3000L, 2500L, 10, ProductStatus.ON_SALE);
        CartAddRequest request = new CartAddRequest(product.getId(), 2);

        // when
        ResultActions result = mockMvc.perform(post("/api/carts")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())   // 변경
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("장바구니 담기 성공"))
                .andExpect(jsonPath("$.data.productId").value(product.getId()))
                .andExpect(jsonPath("$.data.productName").value("사과"))
                .andExpect(jsonPath("$.data.price").value(2500))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(5000));
    }

    @Test
    void 장바구니_상품추가_실패_존재하지않는상품() throws Exception {
        // given
        CartAddRequest request = new CartAddRequest(999999L, 1);

        // when
        ResultActions result = mockMvc.perform(post("/api/carts")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 장바구니_조회_성공() throws Exception {
        // given
        Product product = 상품저장("감자", 4000L, 3000L, 20, ProductStatus.ON_SALE);
        Cart cart = cartRepository.save(Cart.create(user));
        cartItemRepository.save(CartItem.create(cart, product, product.getSalePrice(), 3));

        // when
        ResultActions result = mockMvc.perform(get("/api/carts")
                .header("Authorization", bearerToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 조회 성공"))
                .andExpect(jsonPath("$.data.cartItems.length()").value(1))
                .andExpect(jsonPath("$.data.cartItems[0].productName").value("감자"))
                .andExpect(jsonPath("$.data.totalPrice").value(9000))
                .andExpect(jsonPath("$.data.minOrderAmount").value(20000))
                .andExpect(jsonPath("$.data.isMinOrderAmountMet").value(false)); // 변경
    }

    @Test
    void 장바구니_수량변경_성공() throws Exception {
        // given
        Product product = 상품저장("고구마", 6000L, 5000L, 10, ProductStatus.ON_SALE);
        Cart cart = cartRepository.save(Cart.create(user));
        CartItem cartItem = cartItemRepository.save(
                CartItem.create(cart, product, product.getSalePrice(), 2)
        );
        CartUpdateRequest request = new CartUpdateRequest(4);

        // when
        ResultActions result = mockMvc.perform(patch("/api/carts/{cartItemId}", cartItem.getId())
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 수량 변경 성공"))
                .andExpect(jsonPath("$.data.quantity").value(4))
                .andExpect(jsonPath("$.data.totalPrice").value(20000));

        assertThat(cartItemRepository.findById(cartItem.getId()).orElseThrow().getQuantity()).isEqualTo(4);
    }

    @Test
    void 장바구니_상품삭제_성공() throws Exception {
        // given
        Product product = 상품저장("당근", 3000L, 2500L, 10, ProductStatus.ON_SALE);
        Cart cart = cartRepository.save(Cart.create(user));
        CartItem cartItem = cartItemRepository.save(
                CartItem.create(cart, product, product.getSalePrice(), 2)
        );

        // when
        ResultActions result = mockMvc.perform(delete("/api/carts/items/{cartItemId}", cartItem.getId())
                .header("Authorization", bearerToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 상품 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private Product 상품저장(String name, Long normalPrice, Long salePrice, int stock, ProductStatus status) {
        return productRepository.save(
                Product.create(
                        name,
                        ProductType.NORMAL,
                        normalPrice,
                        salePrice,
                        null,
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