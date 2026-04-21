package com.spartafarmer.agri_commerce.domain.coupon.scheduler;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpireScheduler {

    private final UserCouponRepository userCouponRepository;

    @Scheduled(cron = "0 0 0 * * SAT")  // 매주 토요일 자정에 실행
    @Transactional
    public void expireCoupons() {
        List<UserCoupon> expiredCoupons = userCouponRepository
                .findByStatusAndExpiredAtBefore(CouponStatus.AVAILABLE, LocalDateTime.now());

        for (UserCoupon userCoupon : expiredCoupons) {
            userCoupon.expire();
        }

        log.info("만료 쿠폰 처리 완료 - {}건", expiredCoupons.size());
    }
}
