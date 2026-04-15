package com.spartafarmer.agri_commerce.domain.coupon.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponCreateResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.CouponListResponse;
import com.spartafarmer.agri_commerce.domain.coupon.dto.response.UserCouponResponse;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

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

    // 쿠폰 전체 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public Page<CouponListResponse> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(CouponListResponse::from);
    }

    // 내 쿠폰 목록 조회 (만료 임박순)
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getMyCoupons(Long userId) {
        return userCouponRepository.findByUserIdOrderByExpiredAtAsc(userId).stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}
