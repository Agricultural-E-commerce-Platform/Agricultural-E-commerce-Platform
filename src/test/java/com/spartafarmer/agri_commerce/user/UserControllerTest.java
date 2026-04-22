package com.spartafarmer.agri_commerce.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.user.controller.UserController;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateUserSuccess() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "홍길동",
                "01012345678",
                "서울"
        );

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, List.of())
        );

        doNothing().when(userService).userUpdate(1L, request);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("휴대폰 번호 형식이 잘못되면 회원 정보 수정 실패")
    void updateUserFailWhenInvalidPhone() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "홍길동",
                "0101234",
                "서울"
        );

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, List.of())
        );

        mockMvc.perform(patch("/api/users/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이름이 비어있으면 회원 정보 수정 실패")
    void updateUserFailWhenNameBlank() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "",
                "01012345678",
                "서울"
        );

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, List.of())
        );

        mockMvc.perform(patch("/api/users/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}