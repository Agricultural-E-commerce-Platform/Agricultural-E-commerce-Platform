package com.spartafarmer.agri_commerce.common.exception;

import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.info("클라이언트 오류 - statusCode: {}, message: {}", errorCode.getStatus().value(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(
                        errorCode.getStatus().value(),
                        errorCode.getMessage()
                ));
    }

    // 유효성 검증 실패 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검증에 실패했습니다.");

        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, message));
    }

    // 필수 파라미터 누락 (400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException e) {
        String message = e.getParameterName() + " 파라미터가 필요합니다.";
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, message));
    }

    // @Size 등 제약 조건 검증 실패 (400)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, message));
    }

    // 잘못된 JSON body (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(400, ErrorCode.INVALID_REQUEST.getMessage()));
    }

    // 접근 권한 없음 (403)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ErrorCode.ACCESS_DENIED.getMessage()));
    }

    // 지원하지 않는 HTTP 메서드 (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(405, ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    // DB unique 제약 위반 (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.info("클라이언트 오류 - statusCode: {}, message: {}", HttpStatus.CONFLICT.value(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ErrorCode.DUPLICATE_DATA.getMessage()));
    }

    // 서버 내부 오류 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {

        log.error("서버 오류 발생 - statusCode: 500", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(500, ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
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
