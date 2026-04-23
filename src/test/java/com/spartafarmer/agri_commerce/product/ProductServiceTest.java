package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 상품_목록_조회_성공_전체상품() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Product normalProduct = Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        );
        Product specialProduct = Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        );

        Page<Product> page = new PageImpl<>(List.of(normalProduct, specialProduct), pageable, 2);

        when(productRepository.findByStatusNotOrderByCreatedAtDesc(ProductStatus.HIDDEN, pageable))
                .thenReturn(page);

        // when
        Page<ProductListResponse> result = productService.getProducts(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(ProductListResponse::name)
                .containsExactly("제주 감귤", "딸기 특가");

        verify(productRepository).findByStatusNotOrderByCreatedAtDesc(ProductStatus.HIDDEN, pageable);
        verify(productRepository, never()).findByTypeAndStatusNotOrderByCreatedAtDesc(any(), any(), any());
    }

    @Test
    void 상품_목록_조회_성공_타입별상품() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Product specialProduct1 = Product.create(
                "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null
        );
        Product specialProduct2 = Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        );

        Page<Product> page = new PageImpl<>(List.of(specialProduct1, specialProduct2), pageable, 2);

        when(productRepository.findByTypeAndStatusNotOrderByCreatedAtDesc(
                ProductType.SPECIAL, ProductStatus.HIDDEN, pageable))
                .thenReturn(page);

        // when
        Page<ProductListResponse> result = productService.getProducts(ProductType.SPECIAL, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(p -> p.type() == ProductType.SPECIAL);

        verify(productRepository).findByTypeAndStatusNotOrderByCreatedAtDesc(ProductType.SPECIAL, ProductStatus.HIDDEN, pageable);
        verify(productRepository, never()).findByStatusNotOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void 상품_상세_조회_성공() {
        // given
        Product product = Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        ProductDetailResponse result = productService.getProduct(1L);

        // then
        assertThat(result.name()).isEqualTo("제주 감귤");
        assertThat(result.status()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void 상품_상세_조회_성공_READY상품() {
        // given
        Product readyProduct = Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(readyProduct));

        // when
        ProductDetailResponse result = productService.getProduct(1L);

        // then
        assertThat(result.status()).isEqualTo(ProductStatus.READY);
    }

    @Test
    void 상품_상세_조회_실패_HIDDEN상품() {
        // given
        Product hiddenProduct = Product.create(
                "비공개 상품", ProductType.NORMAL,
                10000L, 10000L, null,
                10, ProductStatus.HIDDEN, null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(hiddenProduct));

        // when & then
        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 상품_상세_조회_실패_없는상품() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 상품_검색_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Product product = Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        );

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByNameContainingAndStatusNotOrderByCreatedAtDesc(
                "감귤", ProductStatus.HIDDEN, pageable))
                .thenReturn(page);

        // when
        Page<ProductListResponse> result = productService.searchProducts("감귤", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("제주 감귤");
    }

    @Test
    void 상품_검색_캐시조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Product product = Product.create(
                "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null
        );

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByNameContainingAndStatusNotOrderByCreatedAtDesc(
                "감귤", ProductStatus.HIDDEN, pageable))
                .thenReturn(page);

        // when
        Page<ProductListResponse> result = productService.searchProductsWithCache("감귤", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("제주 감귤");
    }

    @Test
    void 검색어_정규화_성공_앞뒤공백제거() {
        // given
        String keyword = "   감귤   ";

        // when
        String result = productService.normalizeKeyword(keyword);

        // then
        assertThat(result).isEqualTo("감귤");
    }

    @Test
    void 검색어_정규화_실패_공백만입력() {
        // given
        String keyword = "   ";

        // when & then
        assertThatThrownBy(() -> productService.normalizeKeyword(keyword))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}