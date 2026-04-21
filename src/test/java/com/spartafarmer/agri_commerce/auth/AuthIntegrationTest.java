package com.spartafarmer.agri_commerce.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SigninRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 회원가입_성공() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "test@test.com",
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
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void 회원가입_실패_이메일중복() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "user1@test.com",
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
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("중복된 이메일입니다."));
    }

    @Test
    void 로그인_성공() throws Exception {
        // given
        SigninRequest request = new SigninRequest(
                "user1@test.com",
                "password1"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists()); // 토큰 존재 여부 확인
    }

    @Test
    void 로그인_실패_이메일없음() throws Exception {
        // given
        SigninRequest request = new SigninRequest(
                "user10@test.com",
                "password1"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

    @Test
    void 로그인_실패_비밀번호틀림() throws Exception {
        // given
        SigninRequest request = new SigninRequest(
                "user1@test.com",
                "password2"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 틀렸습니다."));
    }

}
