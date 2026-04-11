package com.spartafarmer.agri_commerce.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotBlank
    private String address;
}
