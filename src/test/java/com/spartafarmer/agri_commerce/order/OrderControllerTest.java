package com.spartafarmer.agri_commerce.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.order.controller.OrderController;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateRequest;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean OrderService orderService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // @AuthenticationPrincipal 주입을 위한 SecurityContext 세팅
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())
        );
    }

    // 다음 테스트에 인증 정보 남지 않도록 초기화
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 주문생성_성공() throws Exception {
        // given
        OrderCreateResponse response = new OrderCreateResponse(
                1L, List.of(), 25000L, 0L, 25000L, "COMPLETED", LocalDateTime.now()
        );
        given(orderService.createOrder(anyLong(), isNull())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderCreateRequest(null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.finalPrice").value(25000))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void 주문생성_실패_장바구니없음() throws Exception {
        // given
        given(orderService.createOrder(anyLong(), isNull()))
                .willThrow(new CustomException(ErrorCode.CART_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderCreateRequest(null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("장바구니를 찾을 수 없습니다."));
    }

    @Test
    void 주문생성_실패_최소주문금액미만() throws Exception {
        // given
        given(orderService.createOrder(anyLong(), isNull()))
                .willThrow(new CustomException(ErrorCode.MIN_ORDER_AMOUNT_NOT_MET));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderCreateRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("최소 주문 금액은 20,000원 이상입니다."));
    }

    @Test
    void 주문목록조회_성공() throws Exception {
        // given
        OrderListResponse response = new OrderListResponse(1L, List.of(), 25000L, "COMPLETED", LocalDateTime.now());
        given(orderService.getOrders(anyLong())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].finalPrice").value(25000));
    }

    @Test
    void 주문목록조회_실패_유저없음() throws Exception {
        // given
        given(orderService.getOrders(anyLong()))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }
}