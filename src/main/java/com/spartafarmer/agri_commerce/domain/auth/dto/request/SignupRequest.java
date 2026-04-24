package com.spartafarmer.agri_commerce.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest (
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이어야 합니다."
        )
        @NotBlank
        String email,

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,12}$",
                message = "비밀번호는 영문, 숫자 포함 8~12자리여야 합니다.")
        @NotBlank
        String password,

        @NotBlank
        String name,

        @Pattern(
                regexp = "^(010-\\d{4}-\\d{4}|010\\d{8})$",
                message = "휴대폰 번호 형식이 올바르지 않습니다. (010-xxxx-xxxx 또는 010xxxxxxxx)"
        )
        @NotBlank
        String phone,

        @NotBlank
        String address
){ }
