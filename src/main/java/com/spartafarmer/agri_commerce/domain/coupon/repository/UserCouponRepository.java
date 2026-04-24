package com.spartafarmer.agri_commerce.domain.coupon.repository;

import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 중복 발급 확인
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    // 본인 소유 쿠폰 단건 조회 (OrderCreateService 에서 사용)
    Optional<UserCoupon> findByIdAndUserId(Long id, Long userId);

    // 사용자 보유 쿠폰 전체 조회 (만료 임박순, Coupon fetch join) -> N+1 해결(Fetch Join)
    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.id = :userId ORDER BY uc.expiredAt ASC")
    List<UserCoupon> findByUserIdOrderByExpiredAtAsc(@Param("userId") Long userId);

    // 만료 처리 벌크 UPDATE -> fromStatus 상태 + 만료시각 지난 쿠폰들을 한 번에 toStatus로 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserCoupon uc SET uc.status = :toStatus " +
            "WHERE uc.status = :fromStatus AND uc.expiredAt < :now")
    int bulkExpire(@Param("fromStatus") CouponStatus fromStatus,
                   @Param("toStatus") CouponStatus toStatus,
                   @Param("now") LocalDateTime now);
}
