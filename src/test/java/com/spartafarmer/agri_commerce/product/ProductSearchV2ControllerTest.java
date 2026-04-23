package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.security.AuthUser;
import com.spartafarmer.agri_commerce.domain.product.controller.ProductSearchV2Controller;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 검색 v2 컨트롤러 단위 테스트")
class ProductSearchV2ControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private PopularSearchService popularSearchService;

    @InjectMocks
    private ProductSearchV2Controller productSearchV2Controller;

    @Test
    @DisplayName("성공: 비로그인 사용자는 인기검색어 집계 없이 검색한다")
    void 상품_검색_V2_성공_비로그인사용자() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        ProductListResponse response = new ProductListResponse(
                1L, "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );

        Page<ProductListResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        given(productService.normalizeKeyword("감귤")).willReturn("감귤");
        given(productService.searchProductsWithCache("감귤", pageable)).willReturn(page);

        // when
        Page<ProductListResponse> result =
                productSearchV2Controller.searchProductsV2("감귤", pageable, null);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("제주 감귤");

        then(productService).should().normalizeKeyword("감귤");
        then(productService).should().searchProductsWithCache("감귤", pageable);
        then(popularSearchService).should(never()).increaseKeyword(anyLong(), anyString());
    }

    @Test
    @DisplayName("성공: 로그인 사용자는 인기검색어를 집계한다")
    void 상품_검색_V2_성공_로그인사용자면_인기검색어집계() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        ProductListResponse response = new ProductListResponse(
                1L, "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );

        Page<ProductListResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);

        given(productService.normalizeKeyword("감귤")).willReturn("감귤");
        given(productService.searchProductsWithCache("감귤", pageable)).willReturn(page);

        // when
        Page<ProductListResponse> result =
                productSearchV2Controller.searchProductsV2("감귤", pageable, authUser);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("제주 감귤");

        then(productService).should().normalizeKeyword("감귤");
        then(productService).should().searchProductsWithCache("감귤", pageable);
        then(popularSearchService).should().increaseKeyword(1L, "감귤");
    }
}