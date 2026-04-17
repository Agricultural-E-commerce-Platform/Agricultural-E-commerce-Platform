package com.spartafarmer.agri_commerce.domain.user.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void userUpdate(Long userId, UserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 하이픈 제거 후 검증
        String formattedPhone = request.phone().replaceAll("-", "");
        if (formattedPhone.length() != 11) {
            throw new CustomException(ErrorCode.USER_INVALID_PHONE);
        }

        // 형식 맞춰서 저장 (010-xxxx-xxxx)
        String savedPhone = formattedPhone.substring(0, 3) + "-"
                + formattedPhone.substring(3, 7) + "-"
                + formattedPhone.substring(7);

        user.update(
                request.name(),
                savedPhone,
                request.address()
        );
    }
}
