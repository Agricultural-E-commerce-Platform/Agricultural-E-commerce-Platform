package com.spartafarmer.agri_commerce.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateRequest;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderCreateResponse;
import com.spartafarmer.agri_commerce.domain.order.dto.OrderListResponse;
import com.spartafarmer.agri_commerce.domain.order.service.OrderService;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;

    @MockitoBean
    OrderService orderService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.create(
                        "order_ctrl@test.com",
                        "Password123",
                        "주문유저",
                        User.formatPhone("01011112222"),
                        "서울시",
                        UserRole.USER
                )
        );
        token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
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
                        .header("Authorization", "Bearer " + token)
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
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderCreateRequest(null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("장바구니를 찾을 수 없습니다."));
    }

    @Test
    void 주문목록조회_성공() throws Exception {
        // given
        OrderListResponse response = new OrderListResponse(1L, List.of(), 25000L, "COMPLETED", LocalDateTime.now());
        given(orderService.getOrders(anyLong())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
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
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }
}