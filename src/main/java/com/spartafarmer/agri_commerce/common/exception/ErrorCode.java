package com.spartafarmer.agri_commerce.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 오류입니다. 점검 후 조치하겠습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 회원
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    USER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    USER_INVALID_LOGIN(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 틀렸습니다."),
    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다. (영문, 숫자 포함 8자리 이상)"),
    USER_INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    USER_INVALID_PHONE(HttpStatus.BAD_REQUEST, "휴대폰 번호 형식이 올바르지 않습니다."),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED, "탈퇴한 회원입니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "현재 판매 중인 상품이 아닙니다."),
    // 주문

    // 장바구니

    // 쿠폰
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "선착순 쿠폰이 모두 소진되었습니다."),
    COUPON_NOT_AVAILABLE_TIME(HttpStatus.BAD_REQUEST, "쿠폰 발급 가능 시간이 아닙니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    COUPON_LOCK_FAILED(HttpStatus.CONFLICT, "잠시 후 다시 시도해주세요."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "보유한 쿠폰을 찾을 수 없습니다."),
    USER_COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    USER_COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),
    COUPON_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, "특가 상품이 포함된 주문에는 쿠폰을 사용할 수 없습니다.");

    // 이 코드 위쪽에 에러 코드 작성
    private final HttpStatus status;
    private final String message;

}
