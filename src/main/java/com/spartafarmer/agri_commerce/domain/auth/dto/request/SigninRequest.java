package com.spartafarmer.agri_commerce.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SigninRequest (
        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
){ }
