package com.spartafarmer.agri_commerce.domain.user.controller;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.user.dto.UserUpdateRequest;
import com.spartafarmer.agri_commerce.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원 정보 변경
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest request) {

        userService.userUpdate(authUser.getId(), request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(200, "회원 정보가 변경되었습니다.", null));
    }
}
