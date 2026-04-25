package com.spartafarmer.agri_commerce.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.cart.controller.CartController;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartAddRequest;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartAddResponse;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartItemResponse;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartResponse;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartUpdateRequest;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartUpdateResponse;
import com.spartafarmer.agri_commerce.domain.cart.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CartService cartService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 장바구니_상품추가_성공() throws Exception {
        CartAddResponse response = new CartAddResponse(
                1L,
                10L,
                "사과",
                2500L,
                2
        );

        given(cartService.addCart(eq(1L), any(CartAddRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartAddRequest(10L, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("장바구니 담기 성공"))
                .andExpect(jsonPath("$.data.cartItemId").value(1))
                .andExpect(jsonPath("$.data.productId").value(10))
                .andExpect(jsonPath("$.data.productName").value("사과"))
                .andExpect(jsonPath("$.data.price").value(2500))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(5000));
    }

    @Test
    void 장바구니_상품추가_실패_상품없음() throws Exception {
        given(cartService.addCart(eq(1L), any(CartAddRequest.class)))
                .willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartAddRequest(999L, 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."));
    }

    @Test
    void 장바구니_조회_성공() throws Exception {
        CartItemResponse item = new CartItemResponse(
                1L,
                10L,
                "감자",
                3000L,
                3,
                ProductStatus.ON_SALE
        );

        CartResponse response = new CartResponse(
                List.of(item),
                9000L,
                20000L,
                false
        );

        given(cartService.getCart(1L)).willReturn(response);

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 조회 성공"))
                .andExpect(jsonPath("$.data.cartItems.length()").value(1))
                .andExpect(jsonPath("$.data.cartItems[0].cartItemId").value(1))
                .andExpect(jsonPath("$.data.cartItems[0].productId").value(10))
                .andExpect(jsonPath("$.data.cartItems[0].productName").value("감자"))
                .andExpect(jsonPath("$.data.cartItems[0].price").value(3000))
                .andExpect(jsonPath("$.data.cartItems[0].quantity").value(3))
                .andExpect(jsonPath("$.data.cartItems[0].totalPrice").value(9000))
                .andExpect(jsonPath("$.data.cartItems[0].productStatus").value("ON_SALE"))
                .andExpect(jsonPath("$.data.totalPrice").value(9000))
                .andExpect(jsonPath("$.data.minOrderAmount").value(20000))
                .andExpect(jsonPath("$.data.isMinOrderAmountMet").value(false));
    }

    @Test
    void 장바구니_조회_실패_장바구니없음() throws Exception {
        given(cartService.getCart(1L))
                .willThrow(new CustomException(ErrorCode.CART_NOT_FOUND));

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("장바구니를 찾을 수 없습니다."));
    }

    @Test
    void 장바구니_수량변경_성공() throws Exception {
        CartUpdateResponse response = new CartUpdateResponse(
                1L,
                10L,
                "고구마",
                5000L,
                4
        );

        given(cartService.updateQuantity(eq(1L), any(CartUpdateRequest.class), eq(1L)))
                .willReturn(response);

        mockMvc.perform(patch("/api/carts/{cartItemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartUpdateRequest(4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 수량 변경 성공"))
                .andExpect(jsonPath("$.data.cartItemId").value(1))
                .andExpect(jsonPath("$.data.productId").value(10))
                .andExpect(jsonPath("$.data.productName").value("고구마"))
                .andExpect(jsonPath("$.data.price").value(5000))
                .andExpect(jsonPath("$.data.quantity").value(4))
                .andExpect(jsonPath("$.data.totalPrice").value(20000));
    }

    @Test
    void 장바구니_수량변경_실패_재고부족() throws Exception {
        given(cartService.updateQuantity(eq(1L), any(CartUpdateRequest.class), eq(1L)))
                .willThrow(new CustomException(ErrorCode.OUT_OF_STOCK));

        mockMvc.perform(patch("/api/carts/{cartItemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartUpdateRequest(999))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("재고가 부족합니다."));
    }

    @Test
    void 장바구니_상품삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/carts/items/{cartItemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 상품 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 장바구니_상품삭제_실패_상품없음_또는_소유자아님() throws Exception {
        doThrow(new CustomException(ErrorCode.CART_ITEM_NOT_FOUND))
                .when(cartService)
                .deleteCartItem(anyLong(), anyLong());

        mockMvc.perform(delete("/api/carts/items/{cartItemId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("장바구니 상품을 찾을 수 없습니다."));
    }
}