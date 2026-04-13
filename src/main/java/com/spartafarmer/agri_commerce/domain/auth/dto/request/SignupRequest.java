package com.spartafarmer.agri_commerce.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @Email
    @NotBlank
    private String email;

    @Size(min = 8, max = 12)
    @NotBlank
    private String password;

    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotBlank
    private String address;
}
