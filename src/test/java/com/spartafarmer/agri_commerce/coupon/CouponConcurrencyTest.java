package com.spartafarmer.agri_commerce.coupon;

import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.service.CouponService;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@Tag("concurrency")
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

        // 150명 더미 유저 일괄 저장 (save() 150회 -> saveAll() 1회로 개선)
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            users.add(User.create(
                    "user" + i + "@test.com",
                    "password1",
                    "테스터" + i,
                    "010-0000-" + String.format("%04d", i),
                    "서울시",
                    UserRole.USER
            ));
        }
        userRepository.saveAll(users);

        this.userIds = userRepository.findAll().stream()
                .map(User::getId)
                .toList();
    }

    @Test
    @DisplayName("100개 수량 쿠폰에 150명이 동시 발급 요청 시 정확히 100개만 발급된다")
    void concurrencyIssueTest() throws InterruptedException {
        int threadCount = 150;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);              // 시작 신호용
        CountDownLatch doneLatch = new CountDownLatch(threadCount);     // 완료 대기용
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            long userId = userIds.get(i);

            executorService.submit(() -> {
                try {
                    startLatch.await();  // 모든 스레드가 여기서 대기

                    boolean done = false;
                    for (int retry = 0; retry < 100; retry++) {
                        try {
                            couponService.issueCoupon(couponId, userId);
                            successCount.incrementAndGet();
                            done = true;
                            break;
                        } catch (CustomException e) {
                            // 락 획득 실패만 재시도, 그 외 비즈니스 예외는 즉시 실패 처리
                            // (기존: 예외 메시지 문자열 매칭 -> ErrorCode 비교로 변경)
                            if (e.getErrorCode() != ErrorCode.LOCK_ACQUIRE_FAILED) {
                                failCount.incrementAndGet();
                                done = true;
                                break;
                            }
                            Thread.sleep(50);
                        }
                    }
                    if (!done) {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();  // 150개 스레드 동시 출발
        doneLatch.await();       // 전부 끝날 때까지 대기
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
