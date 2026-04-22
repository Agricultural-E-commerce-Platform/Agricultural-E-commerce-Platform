package com.spartafarmer.agri_commerce.common;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        String rawSecret = "abcdefghijklmnopqrstuvwxyz123456";
        String encodedSecret = Base64.getEncoder().encodeToString(rawSecret.getBytes());

        ReflectionTestUtils.setField(jwtUtil, "secretKey", encodedSecret);
        jwtUtil.init();
    }

    @Test
    @DisplayName("JWT 생성 성공")
    void createTokenSuccess() {
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Bearer 헤더에서 토큰 추출 성공")
    void extractTokenSuccess() {
        String token = jwtUtil.extractToken("Bearer abc.def.ghi");

        assertThat(token).isEqualTo("abc.def.ghi");
    }

    @Test
    @DisplayName("Bearer 형식이 아니면 null 반환")
    void extractTokenFailWhenInvalidHeader() {
        String token = jwtUtil.extractToken("abc.def.ghi");

        assertThat(token).isNull();
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateTokenSuccess() {
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        boolean result = jwtUtil.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰에서 사용자 정보 추출 성공")
    void getUserInfoFromTokenSuccess() {
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtUtil.getUserEmailFromToken(token)).isEqualTo("test@test.com");
        assertThat(jwtUtil.getUserRoleFromToken(token)).isEqualTo(UserRole.USER);
    }
}