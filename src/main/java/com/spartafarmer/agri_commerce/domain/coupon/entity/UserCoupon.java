package com.spartafarmer.agri_commerce.domain.coupon.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_coupons")
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 쿠폰을 발급받은 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 발급된 쿠폰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    // 쿠폰 만료 시각 (발급일 + 5일 23:59)
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private UserCoupon(User user, Coupon coupon, LocalDateTime expiredAt) {
        this.user = user;
        this.coupon = coupon;
        this.status = CouponStatus.AVAILABLE;
        this.expiredAt = expiredAt;
    }

    // 쿠폰 발급
    public static UserCoupon issue(User user, Coupon coupon, LocalDateTime issuedAt) {
        // 발급일 + 5일 23:59:59
        LocalDateTime expiredAt = issuedAt.toLocalDate().plusDays(5)
                .atTime(23, 59, 59);
        return new UserCoupon(user, coupon, expiredAt);
    }

    // 쿠폰 사용 처리
    public void use() {
        this.status = CouponStatus.USED;
    }

    // 쿠폰 만료 처리 (스케줄러 일괄 처리용)
    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }
}