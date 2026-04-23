package com.spartafarmer.agri_commerce.coupon;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    private Long couponId;
    private List<Long> userIds;

    @BeforeEach
    void setUp() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();

        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(3)
        );
        couponRepository.save(coupon);
        this.couponId = coupon.getId();

        for (int i = 1; i <= 150; i++) {
            userRepository.save(User.create(
                    "user" + i + "@test.com",
                    "password1",
                    "테스터" + i,
                    "010-0000-" + String.format("%04d", i),
                    "서울시",
                    UserRole.USER
            ));
        }

        this.userIds = userRepository.findAll().stream()
                .map(User::getId)
                .toList();
    }

    @Test
    @DisplayName("100개 수량 쿠폰에 150명이 동시 발급 요청 시 정확히 100개만 발급된다")
    void concurrencyIssueTest() throws InterruptedException {
        int threadCount = 150;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            long userId = userIds.get(i);

            executorService.submit(() -> {
                try {
                    // 최대 50번 재시도, 100ms 간격
                    for (int retry = 0; retry < 50; retry++) {
                        try {
                            couponService.issueCoupon(couponId, userId);
                            successCount.incrementAndGet();
                            break;
                        } catch (Exception e) {
                            if (!e.getMessage().contains("요청이 많아")) {
                                failCount.incrementAndGet();
                                break;
                            }
                            Thread.sleep(100);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Coupon result = couponRepository.findById(couponId).orElseThrow();
        long issuedCount = userCouponRepository.count();

        System.out.println("=== 테스트 결과 ===");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("발급된 쿠폰 수: " + issuedCount);
        System.out.println("쿠폰 issuedQuantity: " + result.getIssuedQuantity());

        assertEquals(100, issuedCount);
        assertEquals(100, result.getIssuedQuantity());
    }
}
