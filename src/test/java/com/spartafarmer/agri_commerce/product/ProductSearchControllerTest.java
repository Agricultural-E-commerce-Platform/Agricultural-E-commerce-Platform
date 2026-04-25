package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.product.controller.ProductSearchController;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("상품 검색 v1 컨트롤러 테스트")
class ProductSearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    JwtUtil jwtUtil;

    @Test
    @DisplayName("성공: 상품 검색 v1")
    void 상품_검색_V1_성공() throws Exception {
        // given
        ProductListResponse response = new ProductListResponse(
                1L, "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );

        given(productService.normalizeKeyword("감귤")).willReturn("감귤");
        given(productService.searchProducts(any(), any()))
                .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", "감귤"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상품 검색 성공"))
                .andExpect(jsonPath("$.data.content[0].name").value("제주 감귤"));
    }

    @Test
    @DisplayName("실패: 상품 검색 v1 키워드 길이 초과")
    void 상품_검색_V1_실패_키워드길이초과() throws Exception {
        // given
        String longKeyword = "a".repeat(51);

        // when & then
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", longKeyword))
                .andExpect(status().isBadRequest());

        then(productService).should(never()).normalizeKeyword(any());
        then(productService).should(never()).searchProducts(any(), any());
    }
}