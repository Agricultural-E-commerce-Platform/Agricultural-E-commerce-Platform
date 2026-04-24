package com.spartafarmer.agri_commerce.domain.coupon.scheduler;

import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCreateScheduler {

    // 주간 쿠폰 정책
    private static final String WEEKLY_COUPON_NAME = "주간 선착순 할인 쿠폰";
    private static final Long WEEKLY_COUPON_DISCOUNT_AMOUNT = 5000L;
    private static final int WEEKLY_COUPON_TOTAL_QUANTITY = 50;

    // 발급 가능 시간 - 당일 09:00 ~ 23:59:59
    private static final int ISSUE_START_HOUR = 9;
    private static final int ISSUE_START_MINUTE = 0;
    private static final int ISSUE_END_HOUR = 23;
    private static final int ISSUE_END_MINUTE = 59;
    private static final int ISSUE_END_SECOND = 59;

    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 9 * * MON")
    public void createWeeklyCoupon() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atTime(ISSUE_START_HOUR, ISSUE_START_MINUTE);
        LocalDateTime endTime = today.atTime(ISSUE_END_HOUR, ISSUE_END_MINUTE, ISSUE_END_SECOND);

        Coupon coupon = Coupon.create(
                WEEKLY_COUPON_NAME,
                WEEKLY_COUPON_DISCOUNT_AMOUNT,
                WEEKLY_COUPON_TOTAL_QUANTITY,
                startTime,
                endTime
        );

        couponRepository.save(coupon);
        log.info("주간 쿠폰 자동 생성 완료 - 발급기간: {} ~ {}", startTime, endTime);
    }
}
