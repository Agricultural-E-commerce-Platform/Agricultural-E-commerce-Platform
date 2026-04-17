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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
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

        // 전화번호 포맷팅: DB에 저장할 전화번호 형식 통일 (010-xxxx-xxxx)
        String savedPhone = User.formatPhone(request.phone());

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

        log.info("회원가입 성공 - email: {}", request.email()); // 회원가입 성공 로그
    }


    // 로그인 기능
    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest request) {

        // 이메일로 회원 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("로그인 실패 - email: {}**", request.email().substring(0, 3)); // 로그인 실패 로그
            throw new CustomException(ErrorCode.USER_INVALID_LOGIN);
        }

        log.info("로그인 성공 - email: {}", request.email()); // 로그인 성공 로그

        // 토큰 생성 후 반환
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        return new SigninResponse(bearerToken);
    }
}
