package com.spartafarmer.agri_commerce.auth;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SigninRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.response.SigninResponse;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.spartafarmer.agri_commerce.domain.auth.service.AuthService;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("signup")
    class Signup {

        @Test
        @DisplayName("회원가입 성공")
        void signupSuccess() {
            SignupRequest request = new SignupRequest(
                    "test@test.com",
                    "pass1234",
                    "홍길동",
                    "01012345678",
                    "서울"
            );

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

            authService.signup(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
            assertThat(savedUser.getName()).isEqualTo("홍길동");
            assertThat(savedUser.getPhone()).isEqualTo("010-1234-5678");
            assertThat(savedUser.getAddress()).isEqualTo("서울");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("중복 이메일이면 회원가입 실패")
        void signupFailWhenDuplicateEmail() {
            SignupRequest request = new SignupRequest(
                    "test@test.com",
                    "pass1234",
                    "홍길동",
                    "01012345678",
                    "서울"
            );

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("signin")
    class Signin {

        @Test
        @DisplayName("로그인 성공")
        void signinSuccess() throws Exception {
            SigninRequest request = new SigninRequest("test@test.com", "pass1234");

            User user = User.create(
                    "test@test.com",
                    "encodedPassword",
                    "홍길동",
                    "010-1234-5678",
                    "서울",
                    UserRole.USER
            );
            setId(user, 1L);

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
            when(jwtUtil.createToken(1L, "test@test.com", UserRole.USER)).thenReturn("access-token");

            SigninResponse response = authService.signin(request);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 로그인 실패")
        void signinFailWhenUserNotFound() {
            SigninRequest request = new SigninRequest("none@test.com", "pass1234");

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.signin(request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("비밀번호 불일치면 로그인 실패")
        void signinFailWhenPasswordMismatch() throws Exception {
            SigninRequest request = new SigninRequest("test@test.com", "wrong1234");

            User user = User.create(
                    "test@test.com",
                    "encodedPassword",
                    "홍길동",
                    "010-1234-5678",
                    "서울",
                    UserRole.USER
            );
            setId(user, 1L);

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.signin(request))
                    .isInstanceOf(CustomException.class);

            verify(jwtUtil, never()).createToken(anyLong(), anyString(), any());
        }
    }

    private void setId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}