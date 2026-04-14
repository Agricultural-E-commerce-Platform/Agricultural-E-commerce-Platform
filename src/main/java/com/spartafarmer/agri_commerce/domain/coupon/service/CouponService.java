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

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    // 쿠폰 생성 (관리자)
    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request) {

        // 종료 시각은 시작 시각보다 이후여야 함
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
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
