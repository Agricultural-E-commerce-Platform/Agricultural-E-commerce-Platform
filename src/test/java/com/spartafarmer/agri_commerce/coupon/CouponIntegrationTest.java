package com.spartafarmer.agri_commerce.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.enums.CouponStatus;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.coupon.dto.request.CouponCreateRequest;
import com.spartafarmer.agri_commerce.domain.coupon.entity.Coupon;
import com.spartafarmer.agri_commerce.domain.coupon.entity.UserCoupon;
import com.spartafarmer.agri_commerce.domain.coupon.repository.CouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.repository.UserCouponRepository;
import com.spartafarmer.agri_commerce.domain.coupon.scheduler.CouponCreateScheduler;
import com.spartafarmer.agri_commerce.domain.coupon.scheduler.CouponExpireScheduler;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CouponIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired UserCouponRepository userCouponRepository;
    @Autowired CouponExpireScheduler couponExpireScheduler;
    @Autowired CouponCreateScheduler couponCreateScheduler;

    private User user;
    private User admin;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.create(
                        "coupon_user@test.com",
                        "Password123",
                        "일반유저",
                        User.formatPhone("01012345678"),
                        "서울시 강남구",
                        UserRole.USER
                )
        );

        admin = userRepository.save(
                User.create(
                        "coupon_admin@test.com",
                        "Password123",
                        "관리자",
                        User.formatPhone("01087654321"),
                        "서울시 서초구",
                        UserRole.ADMIN
                )
        );

        userToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        adminToken = jwtUtil.createToken(admin.getId(), admin.getEmail(), admin.getRole());
    }

    // 쿠폰 생성 - 관리자
    @Test
    void 쿠폰_생성_성공() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .header("Authorization", bearerToken(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("쿠폰이 생성되었습니다."))
                .andExpect(jsonPath("$.data.name").value("테스트 쿠폰"))
                .andExpect(jsonPath("$.data.discountAmount").value(5000))
                .andExpect(jsonPath("$.data.totalQuantity").value(100));
    }

    @Test
    void 쿠폰_생성_실패_시작시각이_현재_이전() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .header("Authorization", bearerToken(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("시작 시각은 현재 시각 이후여야 합니다."));
    }

    @Test
    void 쿠폰_생성_실패_종료시각이_시작시각_이전() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .header("Authorization", bearerToken(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("종료 시각은 시작 시각보다 이후여야 합니다."));
    }

    @Test
    void 쿠폰_생성_실패_할인금액_음수() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "음수 쿠폰",
                -1000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = 쿠폰생성요청(request);

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("할인 금액은 1원 이상이어야 합니다."));
    }

    @Test
    void 쿠폰_생성_실패_할인금액_0() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "0원 쿠폰",
                0L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = 쿠폰생성요청(request);

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("할인 금액은 1원 이상이어야 합니다."));
    }

    @Test
    void 쿠폰_생성_실패_일반유저_권한없음() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰",
                5000L,
                100,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/coupons")
                .header("Authorization", bearerToken(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
    }


    // 쿠폰 목록 조회 - 관리자
    @Test
    void 쿠폰_목록_조회_성공() throws Exception {
        // given
        쿠폰저장("쿠폰1", 3000L, 50);
        쿠폰저장("쿠폰2", 5000L, 100);

        // when
        ResultActions result = mockMvc.perform(get("/api/coupons")
                .header("Authorization", bearerToken(adminToken)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void 쿠폰_목록_조회_실패_일반유저_권한없음() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/coupons")
                .header("Authorization", bearerToken(userToken)));

        // then
        result.andExpect(status().isForbidden());
    }

    // 본인 쿠폰 목록 조회
    @Test
    void 내_쿠폰_목록_조회_성공() throws Exception {
        // given
        Coupon coupon1 = 쿠폰저장("쿠폰1", 3000L, 50);
        Coupon coupon2 = 쿠폰저장("쿠폰2", 5000L, 100);
        userCouponRepository.save(UserCoupon.issue(user, coupon1, LocalDateTime.now()));
        userCouponRepository.save(UserCoupon.issue(user, coupon2, LocalDateTime.now().plusDays(1)));

        // when
        ResultActions result = mockMvc.perform(get("/api/coupons/me")
                .header("Authorization", bearerToken(userToken)));

        // then - 만료 임박순 정렬 확인
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].couponName").value("쿠폰1"));
    }

    @Test
    void 내_쿠폰_목록_조회_성공_쿠폰없으면_빈리스트() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/coupons/me")
                .header("Authorization", bearerToken(userToken)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // 쿠폰 발급
    @Test
    void 쿠폰_발급_성공() throws Exception {
        // given
        Coupon coupon = 발급가능한_쿠폰저장("선착순 쿠폰", 5000L, 100);

        // when
        ResultActions result = 쿠폰발급요청(coupon.getId(), userToken);

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("쿠폰이 발급되었습니다."))
                .andExpect(jsonPath("$.data.couponName").value("선착순 쿠폰"))
                .andExpect(jsonPath("$.data.discountAmount").value(5000))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        Coupon savedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(savedCoupon.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    void 쿠폰_발급_실패_존재하지_않는_쿠폰() throws Exception {
        // when
        ResultActions result = 쿠폰발급요청(9999L, userToken);

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("쿠폰을 찾을 수 없습니다."));
    }

    @Test
    void 쿠폰_발급_실패_발급_가능_시간_아님() throws Exception {
        // given - 아직 시작 안 한 쿠폰
        Coupon coupon = 쿠폰저장_시간지정("미래 쿠폰", 5000L, 100,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        // when
        ResultActions result = 쿠폰발급요청(coupon.getId(), userToken);

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("쿠폰 발급 가능 시간이 아닙니다."));
    }

    @Test
    void 쿠폰_발급_실패_중복_발급() throws Exception {
        // given
        Coupon coupon = 발급가능한_쿠폰저장("선착순 쿠폰", 5000L, 100);
        userCouponRepository.save(UserCoupon.issue(user, coupon, LocalDateTime.now()));

        // when
        ResultActions result = 쿠폰발급요청(coupon.getId(), userToken);

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("이미 발급받은 쿠폰입니다."));
    }

    @Test
    void 쿠폰_발급_실패_수량_소진() throws Exception {
        // given - 수량 1개짜리 쿠폰에 이미 1개 발급된 상태
        Coupon coupon = 발급가능한_쿠폰저장("선착순 쿠폰", 5000L, 1);
        coupon.increaseIssuedQuantity();

        // when
        ResultActions result = 쿠폰발급요청(coupon.getId(), userToken);

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("선착순 쿠폰이 모두 소진되었습니다."));
    }

    // 스케줄러
    @Test
    void 만료_쿠폰_일괄_처리_성공() {
        // given - 만료된 UserCoupon 저장
        Coupon coupon = 쿠폰저장("만료 쿠폰", 3000L, 10);
        UserCoupon expiredUserCoupon = userCouponRepository.save(
                UserCoupon.issue(user, coupon, LocalDateTime.now().minusDays(10))
        );

        // when
        couponExpireScheduler.expireCoupons();

        // then
        UserCoupon result = userCouponRepository.findById(expiredUserCoupon.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    void 주간_쿠폰_자동_생성_성공() {
        // given
        long before = couponRepository.count();

        // when
        couponCreateScheduler.createWeeklyCoupon();

        // then
        assertThat(couponRepository.count()).isEqualTo(before + 1);

        List<Coupon> coupons = couponRepository.findAll();
        Coupon created = coupons.get(coupons.size() - 1);
        assertThat(created.getName()).isEqualTo("주간 선착순 할인 쿠폰");
        assertThat(created.getDiscountAmount()).isEqualTo(5000L);
        assertThat(created.getTotalQuantity()).isEqualTo(50);
    }

    // 헬퍼 메서드
    private Coupon 쿠폰저장(String name, Long discountAmount, int totalQuantity) {
        return couponRepository.save(
                Coupon.create(
                        name,
                        discountAmount,
                        totalQuantity,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(3)
                )
        );
    }

    private Coupon 발급가능한_쿠폰저장(String name, Long discountAmount, int totalQuantity) {
        return couponRepository.save(
                Coupon.create(
                        name,
                        discountAmount,
                        totalQuantity,
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1)
                )
        );
    }

    private Coupon 쿠폰저장_시간지정(String name, Long discountAmount, int totalQuantity,
                             LocalDateTime startTime, LocalDateTime endTime) {
        return couponRepository.save(
                Coupon.create(name, discountAmount, totalQuantity, startTime, endTime)
        );
    }

    private ResultActions 쿠폰발급요청(Long couponId, String token) throws Exception {
        return mockMvc.perform(post("/api/coupons/{couponId}/issue", couponId)
                .header("Authorization", bearerToken(token)));
    }

    private ResultActions 쿠폰생성요청(CouponCreateRequest request) throws Exception {
        return mockMvc.perform(post("/api/coupons")
                .header("Authorization", bearerToken(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}