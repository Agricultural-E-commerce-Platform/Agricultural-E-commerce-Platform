package com.spartafarmer.agri_commerce.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SigninResponse {

    private final String accessToken;
    private final String tokenType;

    public SigninResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }
}
