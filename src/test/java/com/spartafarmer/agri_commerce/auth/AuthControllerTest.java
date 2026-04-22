package com.spartafarmer.agri_commerce.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SigninRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.response.SigninResponse;
import com.spartafarmer.agri_commerce.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.spartafarmer.agri_commerce.domain.auth.controller.AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() throws Exception {
        SignupRequest request = new SignupRequest(
                "test@test.com",
                "pass1234",
                "홍길동",
                "01012345678",
                "서울"
        );

        doNothing().when(authService).signup(request);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인 성공")
    void signinSuccess() throws Exception {
        SigninRequest request = new SigninRequest(
                "test@test.com",
                "pass1234"
        );

        when(authService.signin(request)).thenReturn(new SigninResponse("access-token"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}