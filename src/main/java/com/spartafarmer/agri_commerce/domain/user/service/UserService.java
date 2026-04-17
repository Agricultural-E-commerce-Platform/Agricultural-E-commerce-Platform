package com.spartafarmer.agri_commerce.domain.user.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void userUpdate(Long userId, UserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 전화번호 포맷팅: DB에 저장할 전화번호 형식 통일 (010-xxxx-xxxx)
        String savedPhone = User.formatPhone(request.phone());

        user.update(
                request.name(),
                savedPhone,
                request.address()
        );

        log.info("회원정보 수정 성공 - email: {}", user.getEmail());
    }
}
