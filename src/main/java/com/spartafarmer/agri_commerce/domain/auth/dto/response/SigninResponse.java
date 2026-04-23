package com.spartafarmer.agri_commerce.domain.auth.dto.response;


public record SigninResponse (

        String accessToken,
        String tokenType

){
    public SigninResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
