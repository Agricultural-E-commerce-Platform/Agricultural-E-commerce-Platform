package com.spartafarmer.agri_commerce.user;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import com.spartafarmer.agri_commerce.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("userUpdate")
    class UserUpdate {

        @Test
        @DisplayName("회원 정보 수정 성공")
        void userUpdateSuccess() throws Exception {
            // given
            User user = User.create(
                    "test@test.com",
                    "encodedPassword",
                    "기존이름",
                    "010-1111-2222",
                    "기존주소",
                    UserRole.USER
            );
            setId(user, 1L);

            UserUpdateRequest request = new UserUpdateRequest(
                    "새이름",
                    "01012345678",
                    "새주소"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when
            userService.userUpdate(1L, request);

            // then
            assertThat(user.getName()).isEqualTo("새이름");
            assertThat(user.getPhone()).isEqualTo("010-1234-5678");
            assertThat(user.getAddress()).isEqualTo("새주소");
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 수정 실패")
        void userUpdateFailWhenUserNotFound() {
            // given
            UserUpdateRequest request = new UserUpdateRequest(
                    "새이름",
                    "01012345678",
                    "새주소"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.userUpdate(1L, request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("전화번호 형식이 잘못되면 수정 실패")
        void userUpdateFailWhenInvalidPhone() throws Exception {
            // given
            User user = User.create(
                    "test@test.com",
                    "encodedPassword",
                    "기존이름",
                    "010-1111-2222",
                    "기존주소",
                    UserRole.USER
            );
            setId(user, 1L);

            UserUpdateRequest request = new UserUpdateRequest(
                    "새이름",
                    "0101234",
                    "새주소"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.userUpdate(1L, request))
                    .isInstanceOf(CustomException.class);
        }
    }

    private void setId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}