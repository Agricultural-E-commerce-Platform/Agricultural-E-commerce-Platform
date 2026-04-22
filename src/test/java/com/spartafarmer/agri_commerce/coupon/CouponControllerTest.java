package com.spartafarmer.agri_commerce.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.config.SecurityConfig;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.coupon.controller.CouponController;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableMethodSecurity
@Import(SecurityConfig.class)
@DisplayName("쿠폰 컨트롤러 테스트 (API 계층)")
public class CouponControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CouponService couponService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("성공: 관리자 권한(ADMIN)으로 쿠폰 생성 요청 시 201을 반환한다.")
    void createCoupon_ADMIN_권한_성공() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        CouponCreateResponse response = new CouponCreateResponse(
                1L,
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        given(couponService.createCoupon(any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("쿠폰이 생성되었습니다."))
                .andExpect(jsonPath("$.data.name").value("테스트 쿠폰"))
                .andExpect(jsonPath("$.data.discountAmount").value(5000))
                .andExpect(jsonPath("$.data.totalQuantity").value(100));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("실패: 일반 사용자 권한(USER)으로 쿠폰 생성 요청 시 403 Forbidden을 반환한다.")
    void createCoupon_USER_권한_실패() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }
}