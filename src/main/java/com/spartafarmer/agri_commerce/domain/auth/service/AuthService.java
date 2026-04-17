package com.spartafarmer.agri_commerce.domain.auth.service;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SigninRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.request.SignupRequest;
import com.spartafarmer.agri_commerce.domain.auth.dto.response.SigninResponse;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    // 회원가입 기능
    @Transactional
    public void signup(SignupRequest request) {

        // 정책문제
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 하이픈 제거 후 검증
        String formattedPhone = request.phone().replaceAll("-", "");
        if (formattedPhone.length() != 11) {
            throw new CustomException(ErrorCode.USER_INVALID_PHONE);
        }

        // 형식 맞춰서 저장 (010-xxxx-xxxx)
        String savedPhone = formattedPhone.substring(0, 3) + "-"
                + formattedPhone.substring(3, 7) + "-"
                + formattedPhone.substring(7);

        // 유저 생성 및 저장
        User user = User.create(
                request.email(),
                encodedPassword,
                request.name(),
                savedPhone,
                request.address(),
                UserRole.USER // 기본값 설정
        );

        // DB 저장
        userRepository.save(user);
    }


    // 로그인 기능
    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest request) {

        // 이메일로 회원 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_INVALID_LOGIN);
        }

        // 토큰 생성 후 반환
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        return new SigninResponse(bearerToken);
    }
}
