package com.spartafarmer.agri_commerce.domain.coupon.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    // 쿠폰 생성 (관리자)
    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // 시작 시각은 현재 이후여야 함
        if (!request.getStartTime().isAfter(now)) {
            throw new CustomException(ErrorCode.INVALID_COUPON_START_TIME);
        }

        // 종료 시각은 시작 시각보다 이후여야 함
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new CustomException(ErrorCode.INVALID_COUPON_END_TIME);
        }

        Coupon coupon = Coupon.create(
                request.getName(),
                request.getDiscountAmount(),
                request.getTotalQuantity(),
                request.getStartTime(),
                request.getEndTime()
        );

        couponRepository.save(coupon);
        return CouponCreateResponse.from(coupon);
    }
}
