package com.spartafarmer.agri_commerce.domain.coupon.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 쿠폰 이름
    @Column(nullable = false)
    private String name;

    // 정액 할인 금액
    @Column(name = "discount_price", nullable = false)
    private Long discountAmount;

    // 발급 가능 총 수량
    @Column(name = "total_count", nullable = false)
    private int totalQuantity;

    // 현재 발급된 수량 (동시성 제어 대상)
    @Column(name = "issued_count", nullable = false)
    private int issuedQuantity;

    // 쿠폰 발급 시작 시각
    @Column(nullable = false)
    private LocalDateTime startTime;

    // 쿠폰 발급 종료 시각
    @Column(nullable = false)
    private LocalDateTime endTime;

    private Coupon(String name, Long discountAmount, int totalQuantity,
                   LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 쿠폰 생성
    public static Coupon create(String name, Long discountAmount, int totalQuantity,
                                LocalDateTime startTime, LocalDateTime endTime) {
        return new Coupon(name, discountAmount, totalQuantity, startTime, endTime);
    }

    // 발급 수량 증가 (Redis Lock 획득 후 호출)
    public void increaseIssuedQuantity() {
        this.issuedQuantity++;
    }

    // 잔여 수량 확인
    public boolean hasRemaining() {
        return this.issuedQuantity < this.totalQuantity;
    }

    // 발급 가능 시간 확인
    public boolean isAvailableNow(LocalDateTime now) {
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}