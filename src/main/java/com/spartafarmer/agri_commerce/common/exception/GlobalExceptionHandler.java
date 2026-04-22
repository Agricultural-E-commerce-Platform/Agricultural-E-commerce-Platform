package com.spartafarmer.agri_commerce.common.exception;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.info("클라이언트 오류 - statusCode: {}, message: {}", errorCode.getStatus().value(), errorCode.getMessage()); // 4xx 에러 로그

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(
                        errorCode.getStatus().value(),
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검증에 실패했습니다.");

        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message); // 4xx 에러 로그

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, message));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)   // 막힌 api로 유저가 접근 시 403 에러 반환
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "접근 권한이 없습니다."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // DB unique 제약 위반 시 409 반환
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.CONFLICT.value(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "중복된 데이터입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {

        log.error("서버 오류 발생 - statusCode: 500", e); // 5xx 에러 로그

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(500, "서버 오류가 발생했습니다."));
    }

    // 1. keyword 파라미터 자체가 없을 때 (500 → 400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException e) {
        String message = e.getParameterName() + " 파라미터가 필요합니다.";
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, message));
    }

    // 2. 50자 초과 등 @Size 검증 실패할 때 (500 → 400)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, message));
    }

}
