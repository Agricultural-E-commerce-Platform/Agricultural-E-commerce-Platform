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

    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 9 * * MON")  // 매주 월요일 오전 9시에 실행
    public void createWeeklyCoupon() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atTime(9, 0);   // 발급 가능 시각 09시
        LocalDateTime endTime = today.atTime(23, 59, 59);   // 발급 종료 시각 23:59:59

        Coupon coupon = Coupon.create(
                "주간 선착순 할인 쿠폰",
                5000L,  // 할인 금액 5,000원
                50,                   // 수량 50개
                startTime,
                endTime
        );

        couponRepository.save(coupon);
        log.info("주간 쿠폰 자동 생성 완료 - 발급기간: {} ~ {}", startTime, endTime);
    }
}
