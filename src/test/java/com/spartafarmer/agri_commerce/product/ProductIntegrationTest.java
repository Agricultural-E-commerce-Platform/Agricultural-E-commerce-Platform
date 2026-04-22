package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.product.scheduler.TimeSaleEndJob;
import com.spartafarmer.agri_commerce.domain.product.scheduler.TimeSaleStartJob;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleScheduleService;
import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@SpringBootTest
@Transactional
class ProductIntegrationTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private TimeSaleService timeSaleService;
    @Autowired private TimeSaleScheduleService timeSaleScheduleService;
    @Autowired private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        // 일반 상품
        productRepository.save(Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        ));
        // 특가 ON_SALE
        productRepository.save(Product.create(
                "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null
        ));
        // 특가 READY
        productRepository.save(Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        ));
        // HIDDEN
        productRepository.save(Product.create(
                "비공개 상품", ProductType.NORMAL,
                10000L, 10000L, null,
                10, ProductStatus.HIDDEN, null
        ));
    }

    // ===== 상품 목록 조회 =====

    @Test
    void 전체_상품_목록_조회_성공() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<ProductListResponse> result = productService.getProducts(null, pageable);

        // then - HIDDEN 제외, READY 포함 → 3개
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .noneMatch(p -> p.status() == ProductStatus.HIDDEN);
    }

    @Test
    void 특가_상품_목록_조회_HIDDEN_제외_성공() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when - type=SPECIAL이면 HIDDEN만 제외하고 반환 (READY 포함)
        Page<ProductListResponse> result = productService.getProducts(ProductType.SPECIAL, pageable);

        // then - SPECIAL 타입 중 HIDDEN 제외 → ON_SALE + READY = 2개
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .noneMatch(p -> p.status() == ProductStatus.HIDDEN);
    }


    // ===== 상품 상세 조회 =====

    @Test
    void 상품_상세_조회_성공() {
        // given
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ON_SALE)
                .findFirst().get();

        // when
        ProductDetailResponse result = productService.getProduct(product.getId());

        // then
        assertThat(result.name()).isEqualTo("제주 감귤");
    }

    @Test
    void 상품_상세_조회_READY_상품_조회_성공() {
        // given
        Product readyProduct = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.READY)
                .findFirst().get();

        // when
        ProductDetailResponse result = productService.getProduct(readyProduct.getId());

        // then - READY 상태도 상세 조회 가능
        assertThat(result.status()).isEqualTo(ProductStatus.READY);
    }

    @Test
    void 상품_상세_조회_실패_HIDDEN_상품() {
        // given
        Product hiddenProduct = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.HIDDEN)
                .findFirst().get();

        // when & then
        assertThatThrownBy(() -> productService.getProduct(hiddenProduct.getId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 상품_상세_조회_실패_없는상품() {
        // when & then
        assertThatThrownBy(() -> productService.getProduct(9999L))
                .isInstanceOf(CustomException.class);
    }

    // ===== 타임세일 =====

    @Test
    void 타임세일_시작_재고있으면_ON_SALE_성공() {
        // given
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.READY)
                .findFirst().get();

        // when
        timeSaleService.startProductSale(product.getId());

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void 타임세일_시작_재고없으면_SOLD_OUT_성공() {
        // given - 재고 0인 READY 상품 저장
        Product product = productRepository.save(Product.create(
                "재고없는 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                0, ProductStatus.READY, null
        ));

        // when
        timeSaleService.startProductSale(product.getId());

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void 타임세일_종료_SALE_ENDED_성공() {
        // given
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ON_SALE
                        && p.getType() == ProductType.SPECIAL)
                .findFirst().get();

        // when
        timeSaleService.endProductSale(product.getId());

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE_ENDED);
    }

    @Test
    void Quartz_Job_등록_성공() throws Exception {
        // given
        Long productId = 999L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        // when
        timeSaleScheduleService.scheduleJob(TimeSaleStartJob.class, productId, scheduleTime);

        // then
        JobKey jobKey = JobKey.jobKey("TimeSaleStartJob-" + productId);
        assertThat(scheduler.checkExists(jobKey)).isTrue();
    }

    @Test
    void Quartz_Job_중복_등록_방지_성공() throws Exception {
        // given
        Long productId = 998L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        // when - 같은 productId로 두 번 호출
        timeSaleScheduleService.scheduleJob(TimeSaleEndJob.class, productId, scheduleTime);
        timeSaleScheduleService.scheduleJob(TimeSaleEndJob.class, productId, scheduleTime);

        // then - 트리거 1개만 존재
        JobKey jobKey = JobKey.jobKey("TimeSaleEndJob-" + productId);
        assertThat(scheduler.getTriggersOfJob(jobKey)).hasSize(1);
    }
}