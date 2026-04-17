package com.spartafarmer.agri_commerce.common.config;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local") // 로컬 환경에서만 실행 -> local 프로파일로 실행할 때만 생성됨
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("더미데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("더미데이터 생성을 시작합니다.");
        createUsers();
        createProducts();
        createCoupons();
        log.info("더미데이터 생성이 완료되었습니다.");
    }

    private void createUsers() {
        List<User> users = List.of(

                // 관리자
                User.create("admin@test.com", passwordEncoder.encode("password1"),
                        "관리자", "010-0000-0000", "서울시 강남구", UserRole.ADMIN),

                // 일반 유저 3명
                User.create("user1@test.com", passwordEncoder.encode("password1"),
                        "김농부", "010-1111-1111", "경기도 수원시", UserRole.USER),
                User.create("user2@test.com", passwordEncoder.encode("password1"),
                        "이과일", "010-2222-2222", "충남 논산시", UserRole.USER),
                User.create("user3@test.com", passwordEncoder.encode("password1"),
                        "박채소", "010-3333-3333", "전남 나주시", UserRole.USER)
        );

        userRepository.saveAll(users);
        log.info("유저 4명 생성 완료 (관리자 1 + 일반 3)");
    }

    private void createProducts() {
        List<Product> products = List.of(
                // 일반 상품 10개 (판매중)
                Product.create("유기농 감자 3kg", ProductType.NORMAL, 12000L, 10000L, null, 100, ProductStatus.ON_SALE, null),
                Product.create("제주 당근 2kg", ProductType.NORMAL, 8000L, 6500L, null, 150, ProductStatus.ON_SALE, null),
                Product.create("친환경 양파 5kg", ProductType.NORMAL, 15000L, 12000L, null, 200, ProductStatus.ON_SALE, null),
                Product.create("국내산 고구마 3kg", ProductType.NORMAL, 18000L, 15000L, null, 80, ProductStatus.ON_SALE, null),
                Product.create("무농약 브로콜리 2송이", ProductType.NORMAL, 6000L, 5000L, null, 120, ProductStatus.ON_SALE, null),
                Product.create("유기농 토마토 1kg", ProductType.NORMAL, 9000L, 7500L, null, 90, ProductStatus.ON_SALE, null),
                Product.create("국내산 시금치 300g", ProductType.NORMAL, 4000L, 3500L, null, 200, ProductStatus.ON_SALE, null),
                Product.create("친환경 파프리카 3개", ProductType.NORMAL, 7000L, 6000L, null, 110, ProductStatus.ON_SALE, null),
                Product.create("무농약 상추 200g", ProductType.NORMAL, 3000L, 2500L, null, 180, ProductStatus.ON_SALE, null),
                Product.create("국내산 대파 1단", ProductType.NORMAL, 5000L, 4000L, null, 160, ProductStatus.ON_SALE, null),

                // 특가 상품 5개 (판매중)
                Product.create("[타임세일] 프리미엄 딸기 500g", ProductType.SPECIAL, 20000L, 15000L, 8000L, 50, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 한라봉 2kg", ProductType.SPECIAL, 30000L, 25000L, 15000L, 30, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 유기농 사과 3kg", ProductType.SPECIAL, 25000L, 20000L, 12000L, 40, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 제주 감귤 5kg", ProductType.SPECIAL, 22000L, 18000L, 10000L, 60, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 친환경 포도 2kg", ProductType.SPECIAL, 28000L, 22000L, 14000L, 35, ProductStatus.ON_SALE, null),

                // 테스트용 — 재고 0 (판매중이지만 재고 없음)
                Product.create("재고없는 바나나 1송이", ProductType.NORMAL, 5000L, 4000L, null, 0, ProductStatus.ON_SALE, null),

                // 테스트용 — 품절 상태
                Product.create("[품절] 유기농 블루베리 200g", ProductType.NORMAL, 15000L, 12000L, null, 0, ProductStatus.SOLD_OUT, null),

                // 테스트용 — 판매 종료 상태
                Product.create("[판매종료] 한정판 수박", ProductType.SPECIAL, 30000L, 25000L, 15000L, 10, ProductStatus.SALE_ENDED, null)
        );

        productRepository.saveAll(products);
        log.info("상품 18개 생성 완료 (일반 10 + 특가 5 + 테스트용 3)");
    }

    private void createCoupons() {
        LocalDateTime now = LocalDateTime.now();

        List<Coupon> coupons = List.of(
                // 진행 중인 쿠폰
                Coupon.create("선착순 5천원 할인 쿠폰", 5000L, 100, now.minusDays(1), now.plusDays(3)),

                // 시작 전 쿠폰
                Coupon.create("다음주 3천원 할인 쿠폰", 3000L, 50, now.plusDays(3), now.plusDays(7)),

                // 종료된 쿠폰
                Coupon.create("지난주 2천원 할인 쿠폰", 2000L, 30, now.minusDays(7), now.minusDays(1))
        );

        couponRepository.saveAll(coupons);
        log.info("쿠폰 3개 생성 완료 (진행 중 1 + 시작 전 1 + 종료 1)");
    }
}
