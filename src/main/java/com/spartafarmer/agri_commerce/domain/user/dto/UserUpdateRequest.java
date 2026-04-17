package com.spartafarmer.agri_commerce.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest (
        @NotBlank
        String name,

        @Pattern(regexp = "^010(\\d{8}|(-\\d{4}){2})$",
                message = "휴대폰 번호 형식이 올바르지 않습니다. (010-xxxx-xxxx 또는 010xxxxxxxx)")
        @NotBlank
        String phone,

        @NotBlank
        String address
){ }
