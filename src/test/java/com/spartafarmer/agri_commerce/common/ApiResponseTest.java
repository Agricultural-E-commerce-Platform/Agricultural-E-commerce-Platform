package com.spartafarmer.agri_commerce.common;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("success(data) 응답 생성 성공")
    void successWithDataOnly() {
        String data = "테스트 데이터";

        ApiResponse<String> response = ApiResponse.success(data);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("success(status, message, data) 응답 생성 성공")
    void successWithCustomStatusMessageAndData() {
        String data = "테스트 데이터";

        ApiResponse<String> response = ApiResponse.success(201, "생성 성공", data);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getMessage()).isEqualTo("생성 성공");
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("error(status, message) 응답 생성 성공")
    void errorResponseCreate() {
        ApiResponse<Void> response = ApiResponse.error(400, "잘못된 요청입니다.");

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("잘못된 요청입니다.");
        assertThat(response.getData()).isNull();
    }
}