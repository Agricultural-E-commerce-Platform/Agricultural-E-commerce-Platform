package com.spartafarmer.agri_commerce.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        SignupRequest request = new SignupRequest(
                "testuser@test.com",
                "password1",
                "테스트유저",
                "010-1234-5678",
                "서울시 강남구"
        );

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String getToken() {
        User user = userRepository.findByEmail("testuser@test.com")
                .orElseThrow();
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    void 회원정보_수정_성공() throws Exception {
        // given
        String token = getToken();

        UserUpdateRequest request = new UserUpdateRequest(
                "김아무개",
                "01087654321",
                "경기도 남양주시"
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void 회원정보_수정_실패_토큰_없음() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest(
                "김아무개",
                "01087654321",
                "경기도 남양주시"
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void 회원정보_수정_실패_유저_없음() throws Exception {
        // given
        // 존재하지 않는 userId로 토큰 생성
        String token = jwtUtil.createToken(Long.MAX_VALUE, "ghost@test.com", UserRole.USER);

        UserUpdateRequest request = new UserUpdateRequest(
                "김아무개",
                "01087654321",
                "경기도 남양주시"
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

}
