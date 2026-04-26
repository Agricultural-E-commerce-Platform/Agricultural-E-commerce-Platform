package com.spartafarmer.agri_commerce.common.config;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartItemRepository;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserCouponRepository userCouponRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("더미데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("더미데이터 생성을 시작합니다.");
        List<User> users = createUsers();
        List<Product> products = createProducts();
        List<Coupon> coupons = createCoupons();
        createCartsAndUserCoupons(users, products, coupons);
        log.info("더미데이터 생성이 완료되었습니다.");
    }

    // ===== 유저 생성 =====
    private List<User> createUsers() {
        List<User> users = userRepository.saveAll(List.of(

                // 관리자 (TC-065, TC-066, TC-073, TC-074 등 관리자 권한 테스트용)
                User.create("admin@test.com", passwordEncoder.encode("password1"),
                        "관리자", "010-0000-0000", "서울시 강남구", UserRole.ADMIN),

                // user1 - 일반 유저 (주문/장바구니 기본 테스트용)
                User.create("user1@test.com", passwordEncoder.encode("password1"),
                        "김농부", "010-1111-1111", "경기도 수원시", UserRole.USER),

                // user2 - USED 상태 쿠폰 보유 유저 (TC-093 이미 사용된 쿠폰 테스트용)
                User.create("user2@test.com", passwordEncoder.encode("password1"),
                        "이과일", "010-2222-2222", "충남 논산시", UserRole.USER),

                // user3 - EXPIRED 상태 쿠폰 보유 유저 (TC-092 만료된 쿠폰 테스트용)
                User.create("user3@test.com", passwordEncoder.encode("password1"),
                        "박채소", "010-3333-3333", "전남 나주시", UserRole.USER),

                // user4 - 품절 상품 장바구니 + AVAILABLE/EXPIRED 쿠폰 보유 (TC-047 품절 상품 주문 테스트용)
                User.create("user4@test.com", passwordEncoder.encode("password1"),
                        "김계란", "010-4444-4444", "경남 창원시", UserRole.USER),

                // user5 - 판매종료 상품 장바구니 + AVAILABLE/EXPIRED 쿠폰 보유 (TC-048 판매종료 상품 주문 테스트용)
                User.create("user5@test.com", passwordEncoder.encode("password1"),
                        "김태연", "010-5555-5555", "전북 전주시", UserRole.USER),

                // user6 - 품절+판매종료 상품 장바구니 + AVAILABLE/EXPIRED 쿠폰 보유
                User.create("user6@test.com", passwordEncoder.encode("password1"),
                        "손예진", "010-6666-6666", "대구시 달서구", UserRole.USER),

                // user7 - 빈 장바구니 유저 (TC-056 장바구니 없음 테스트용)
                User.create("user7@test.com", passwordEncoder.encode("password1"),
                        "권정열", "010-7777-7777", "경북 구미시", UserRole.USER),

                // user8 - 주문 없는 유저 (TC-101 주문 목록 빈 목록 테스트용)
                User.create("user8@test.com", passwordEncoder.encode("password1"),
                        "이현중", "010-8888-8888", "경기도 용인시", UserRole.USER)
        ));

        log.info("유저 9명 생성 완료 (관리자 1 + 일반 8)");
        return users;
    }

    // ===== 상품 생성 =====
    private List<Product> createProducts() {
        List<Product> products = new ArrayList<>(productRepository.saveAll(List.of(

                // 일반 상품 10개 (판매중) - TC-029, TC-030, TC-086 등 기본 테스트용
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

                // 특가 상품 5개 (판매중) - TC-031, TC-091 특가 상품 테스트용
                Product.create("[타임세일] 프리미엄 딸기 500g", ProductType.SPECIAL, 20000L, 15000L, 8000L, 50, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 한라봉 2kg", ProductType.SPECIAL, 30000L, 25000L, 15000L, 30, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 유기농 사과 3kg", ProductType.SPECIAL, 25000L, 20000L, 12000L, 40, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 제주 감귤 5kg", ProductType.SPECIAL, 22000L, 18000L, 10000L, 60, ProductStatus.ON_SALE, null),
                Product.create("[타임세일] 친환경 포도 2kg", ProductType.SPECIAL, 28000L, 22000L, 14000L, 35, ProductStatus.ON_SALE, null),

                // 테스트용 — 재고 0 (판매중이지만 재고 없음)
                Product.create("재고없는 바나나 1송이", ProductType.NORMAL, 5000L, 4000L, null, 0, ProductStatus.ON_SALE, null),

                // 테스트용 — 품절 상태 (TC-047, TC-084, TC-095용)
                Product.create("[품절] 유기농 블루베리 200g", ProductType.NORMAL, 15000L, 12000L, null, 0, ProductStatus.SOLD_OUT, null),

                // 테스트용 — 판매 종료 상태 (TC-048용)
                Product.create("[판매종료] 한정판 수박", ProductType.SPECIAL, 30000L, 25000L, 15000L, 10, ProductStatus.SALE_ENDED, null),

                // 테스트용 — 재고 5개 (TC-050, TC-060 재고 초과 테스트용)
                Product.create("재고 5개 오이 1kg", ProductType.NORMAL, 5000L, 4000L, null, 5, ProductStatus.ON_SALE, null),

                // 테스트용 — 판매 준비 상태 READY (TC-049용)
                Product.create("[준비중] 한정판 멜론", ProductType.SPECIAL, 20000L, 15000L, 10000L, 50, ProductStatus.READY, null)
        )));

        log.info("상품 20개 생성 완료 (일반 10 + 특가 5 + 테스트용 5)");

        // 테스트 상품 50000개 - 청크 단위로 저장 (검색 성능 테스트용)
        List<Product> testProducts = new ArrayList<>();
        for (int i = 1; i <= 50000; i++) {
            testProducts.add(Product.create(
                    "테스트 상품 " + i,
                    ProductType.NORMAL,
                    10000L, 8000L, null,
                    100, ProductStatus.ON_SALE, null
            ));
            if (i % 1000 == 0) {
                productRepository.saveAll(testProducts);
                testProducts.clear();
                log.info("테스트 상품 {}개 저장 완료", i);
            }
        }
        log.info("테스트 상품 50000개 생성 완료");
        return products;
    }

    // ===== 쿠폰 생성 =====
    private List<Coupon> createCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 수량 소진 쿠폰 생성 후 issuedQuantity 채우기 (TC-084 선착순 소진 테스트용)
        Coupon soldOutCoupon = Coupon.create("선착순 마감 쿠폰", 1000L, 10,
                now.minusDays(1), now.plusDays(3));
        for (int i = 0; i < 10; i++) {
            soldOutCoupon.increaseIssuedQuantity();
        }

        List<Coupon> coupons = couponRepository.saveAll(List.of(
                // 진행 중인 쿠폰 (TC-079 정상 발급 테스트용)
                Coupon.create("선착순 5천원 할인 쿠폰", 5000L, 100, now.minusDays(1), now.plusDays(3)),

                // 시작 전 쿠폰 (TC-082 발급 시작 전 테스트용)
                Coupon.create("다음주 3천원 할인 쿠폰", 3000L, 50, now.plusDays(3), now.plusDays(7)),

                // 종료된 쿠폰 (TC-083 발급 종료 후 테스트용)
                Coupon.create("지난주 2천원 할인 쿠폰", 2000L, 30, now.minusDays(7), now.minusDays(1)),

                // 수량 소진 쿠폰 (TC-084 선착순 소진 테스트용)
                soldOutCoupon,

                // user2 USED 쿠폰용 (TC-093 이미 사용된 쿠폰 테스트용)
                Coupon.create("테스트용 쿠폰 A", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user3 EXPIRED 쿠폰용 (TC-092 만료된 쿠폰 테스트용)
                Coupon.create("테스트용 쿠폰 B", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user4 AVAILABLE 쿠폰용
                Coupon.create("테스트용 쿠폰 C", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user4 EXPIRED 쿠폰용
                Coupon.create("테스트용 쿠폰 D", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user5 AVAILABLE 쿠폰용
                Coupon.create("테스트용 쿠폰 E", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user5 EXPIRED 쿠폰용
                Coupon.create("테스트용 쿠폰 F", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user6 AVAILABLE 쿠폰용
                Coupon.create("테스트용 쿠폰 G", 1000L, 100, now.minusDays(1), now.plusDays(3)),

                // user6 EXPIRED 쿠폰용
                Coupon.create("테스트용 쿠폰 H", 1000L, 100, now.minusDays(1), now.plusDays(3))
        ));

        log.info("쿠폰 12개 생성 완료");
        return coupons;
    }

    // ===== 장바구니 + UserCoupon 생성 =====
    private void createCartsAndUserCoupons(List<User> users, List<Product> products, List<Coupon> coupons) {
        LocalDateTime now = LocalDateTime.now();

        // 유저 (인덱스 기준: 0=admin, 1=user1 ... )
        User user2 = users.get(2); // USED 쿠폰 보유
        User user3 = users.get(3); // EXPIRED 쿠폰 보유
        User user4 = users.get(4); // 품절 상품 장바구니
        User user5 = users.get(5); // 판매종료 상품 장바구니
        User user6 = users.get(6); // 품절+판매종료 장바구니
        User user7 = users.get(7); // 빈 장바구니

        // 상품 (인덱스 기준)
        Product soldOutProduct = products.get(16);   // [품절] 유기농 블루베리
        Product saleEndedProduct = products.get(17); // [판매종료] 한정판 수박

        // 쿠폰 (인덱스 기준: 0=선착순5천원, 1=다음주3천원, 2=지난주2천원, 3=마감쿠폰, 4=A~)
        Coupon couponA = coupons.get(4);  // user2 USED용
        Coupon couponB = coupons.get(5);  // user3 EXPIRED용
        Coupon couponC = coupons.get(6);  // user4 AVAILABLE용
        Coupon couponD = coupons.get(7);  // user4 EXPIRED용
        Coupon couponE = coupons.get(8);  // user5 AVAILABLE용
        Coupon couponF = coupons.get(9);  // user5 EXPIRED용
        Coupon couponG = coupons.get(10); // user6 AVAILABLE용
        Coupon couponH = coupons.get(11); // user6 EXPIRED용

        // user2 - USED 상태 쿠폰 (TC-093 이미 사용된 쿠폰 테스트용)
        UserCoupon uc2Used = userCouponRepository.save(UserCoupon.issue(user2, couponA, now));
        uc2Used.use();

        // user3 - EXPIRED 상태 쿠폰 (TC-092 만료된 쿠폰 테스트용)
        UserCoupon uc3Expired = userCouponRepository.save(UserCoupon.issue(user3, couponB, now.minusDays(10)));
        uc3Expired.expire();

        // user4 - 품절 상품 장바구니 + AVAILABLE/EXPIRED 쿠폰
        Cart cart4 = cartRepository.save(Cart.create(user4));
        cartItemRepository.save(CartItem.create(cart4, soldOutProduct, soldOutProduct.getSalePrice(), 1));
        userCouponRepository.save(UserCoupon.issue(user4, couponC, now));                          // AVAILABLE
        UserCoupon uc4Expired = userCouponRepository.save(UserCoupon.issue(user4, couponD, now.minusDays(10)));
        uc4Expired.expire();                                                                        // EXPIRED

        // user5 - 판매종료 상품 장바구니 + AVAILABLE/EXPIRED 쿠폰
        Cart cart5 = cartRepository.save(Cart.create(user5));
        cartItemRepository.save(CartItem.create(cart5, saleEndedProduct, saleEndedProduct.getSalePrice(), 1));
        userCouponRepository.save(UserCoupon.issue(user5, couponE, now));                          // AVAILABLE
        UserCoupon uc5Expired = userCouponRepository.save(UserCoupon.issue(user5, couponF, now.minusDays(10)));
        uc5Expired.expire();                                                                        // EXPIRED

        // user6 - 품절+판매종료 장바구니 + AVAILABLE/EXPIRED 쿠폰
        Cart cart6 = cartRepository.save(Cart.create(user6));
        cartItemRepository.save(CartItem.create(cart6, soldOutProduct, soldOutProduct.getSalePrice(), 1));
        cartItemRepository.save(CartItem.create(cart6, saleEndedProduct, saleEndedProduct.getSalePrice(), 1));
        userCouponRepository.save(UserCoupon.issue(user6, couponG, now));                          // AVAILABLE
        UserCoupon uc6Expired = userCouponRepository.save(UserCoupon.issue(user6, couponH, now.minusDays(10)));
        uc6Expired.expire();                                                                        // EXPIRED

        // user7 - 빈 장바구니 (TC-056 장바구니 없음 테스트용)
        cartRepository.save(Cart.create(user7));

        log.info("장바구니 및 UserCoupon 생성 완료");
    }
}
