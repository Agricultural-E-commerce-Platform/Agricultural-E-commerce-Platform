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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@SpringBootTest(properties = {
        "jwt.secret.key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Transactional
class ProductIntegrationTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private TimeSaleService timeSaleService;
    @Autowired private TimeSaleScheduleService timeSaleScheduleService;
    @Autowired private Scheduler scheduler;

    private Product normalProduct;
    private Product onSaleSpecialProduct;
    private Product readySpecialProduct;
    private Product hiddenProduct;

    @BeforeEach
    void setUp() {
        // 일반 상품
        normalProduct = productRepository.save(Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        ));

        // 특가 ON_SALE
        onSaleSpecialProduct = productRepository.save(Product.create(
                "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null
        ));

        // 특가 READY
        readySpecialProduct = productRepository.save(Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        ));

        // HIDDEN
        hiddenProduct = productRepository.save(Product.create(
                "비공개 상품", ProductType.NORMAL,
                10000L, 10000L, null,
                10, ProductStatus.HIDDEN, null
        ));

        // 내가 만든 테스트 데이터가 첫 페이지 최상단에 오도록 createdAt을 미래로 설정
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        ReflectionTestUtils.setField(normalProduct, "createdAt", future.plusSeconds(1));
        ReflectionTestUtils.setField(onSaleSpecialProduct, "createdAt", future.plusSeconds(2));
        ReflectionTestUtils.setField(readySpecialProduct, "createdAt", future.plusSeconds(3));
        ReflectionTestUtils.setField(hiddenProduct, "createdAt", future.plusSeconds(4));
    }

    @Test
    void 전체_상품_목록_조회_성공() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<ProductListResponse> result = productService.getProducts(null, pageable);

        List<String> names = result.getContent().stream()
                .map(ProductListResponse::name)
                .toList();

        assertThat(names).contains("제주 감귤", "한우 특가", "딸기 특가");
        assertThat(names).doesNotContain("비공개 상품");

        assertThat(result.getContent())
                .noneMatch(p -> p.status() == ProductStatus.HIDDEN);
    }

    @Test
    void 특가_상품_목록_조회_HIDDEN_제외_성공() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<ProductListResponse> result = productService.getProducts(ProductType.SPECIAL, pageable);

        List<String> names = result.getContent().stream()
                .map(ProductListResponse::name)
                .toList();

        assertThat(names).contains("한우 특가", "딸기 특가");
        assertThat(names).doesNotContain("비공개 상품");

        assertThat(result.getContent())
                .allMatch(p -> p.type() == ProductType.SPECIAL);

        assertThat(result.getContent())
                .noneMatch(p -> p.status() == ProductStatus.HIDDEN);
    }

    @Test
    void 상품_상세_조회_성공() {
        ProductDetailResponse result = productService.getProduct(normalProduct.getId());

        assertThat(result.name()).isEqualTo("제주 감귤");
    }

    @Test
    void 상품_상세_조회_READY_상품_조회_성공() {
        ProductDetailResponse result = productService.getProduct(readySpecialProduct.getId());

        assertThat(result.status()).isEqualTo(ProductStatus.READY);
    }

    @Test
    void 상품_상세_조회_실패_HIDDEN_상품() {
        assertThatThrownBy(() -> productService.getProduct(hiddenProduct.getId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 상품_상세_조회_실패_없는상품() {
        Long notExistId = Long.MAX_VALUE;

        assertThatThrownBy(() -> productService.getProduct(notExistId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 타임세일_시작_재고있으면_ON_SALE_성공() {
        timeSaleService.startProductSale(readySpecialProduct.getId());

        assertThat(readySpecialProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void 타임세일_시작_재고없으면_SOLD_OUT_성공() {
        Product zeroStockReadyProduct = productRepository.save(Product.create(
                "재고없는 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                0, ProductStatus.READY, null
        ));

        timeSaleService.startProductSale(zeroStockReadyProduct.getId());

        assertThat(zeroStockReadyProduct.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void 타임세일_종료_SALE_ENDED_성공() {
        timeSaleService.endProductSale(onSaleSpecialProduct.getId());

        assertThat(onSaleSpecialProduct.getStatus()).isEqualTo(ProductStatus.SALE_ENDED);
    }

    @Test
    void Quartz_Job_등록_성공() throws Exception {
        Long productId = 999L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        timeSaleScheduleService.scheduleJob(TimeSaleStartJob.class, productId, scheduleTime);

        JobKey jobKey = JobKey.jobKey("TimeSaleStartJob-" + productId);
        assertThat(scheduler.checkExists(jobKey)).isTrue();
    }

    @Test
    void Quartz_Job_중복_등록_방지_성공() throws Exception {
        Long productId = 998L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        timeSaleScheduleService.scheduleJob(TimeSaleEndJob.class, productId, scheduleTime);
        timeSaleScheduleService.scheduleJob(TimeSaleEndJob.class, productId, scheduleTime);

        JobKey jobKey = JobKey.jobKey("TimeSaleEndJob-" + productId);
        assertThat(scheduler.getTriggersOfJob(jobKey)).hasSize(1);
    }
}