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

        user.update(
                request.getName(),
                request.getPhone(),
                request.getAddress()
        );
    }
}
