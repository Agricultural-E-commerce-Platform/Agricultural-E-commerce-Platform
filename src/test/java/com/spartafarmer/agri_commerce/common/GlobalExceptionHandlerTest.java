package com.spartafarmer.agri_commerce.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void CustomException_처리_확인() throws Exception {
        // given - 존재하지 않는 상품 ID

        // when
        ResultActions result = mockMvc.perform(get("/api/products/" + Long.MAX_VALUE));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."));
    }

    @Test
    void MethodArgumentNotValidException_처리_확인() throws Exception {
        // given - 이메일 형식 잘못된 요청
        SignupRequest request = new SignupRequest(
                "잘못된이메일",
                "password1",
                "테스트",
                "010-1234-5678",
                "서울시 강남구"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void MissingServletRequestParameterException_처리_확인() throws Exception {
        // given - keyword 파라미터 없음

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/search"));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

    }

    @Test
    void ConstraintViolationException_처리_확인() throws Exception {
        // given - keyword 50자 초과
        String longKeyword = "a".repeat(51);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", longKeyword));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

    }

}
