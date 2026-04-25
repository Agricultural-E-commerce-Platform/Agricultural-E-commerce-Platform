package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.product.controller.ProductController;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductDetailResponse;
import com.spartafarmer.agri_commerce.domain.product.dto.ProductListResponse;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    JwtUtil jwtUtil;

    @Test
    @DisplayName("성공: 전체 상품 목록 조회")
    void 상품_목록_조회_성공() throws Exception {
        // given
        ProductListResponse response1 = new ProductListResponse(
                1L, "제주 감귤", ProductType.NORMAL,
                15000L, 12000L, null,
                100, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );
        ProductListResponse response2 = new ProductListResponse(
                2L, "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );

        given(productService.getProducts(eq(null), any()))
                .willReturn(new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 10), 2));

        // when & then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].name").value("제주 감귤"))
                .andExpect(jsonPath("$.data.content[0].type").value("NORMAL"))
                .andExpect(jsonPath("$.data.content[1].name").value("한우 특가"))
                .andExpect(jsonPath("$.data.content[1].type").value("SPECIAL"))
                .andExpect(jsonPath("$.message").value("상품 목록 조회 성공"));
    }

    @Test
    @DisplayName("성공: 특가 상품 목록 조회")
    void 특가_상품_목록_조회_성공() throws Exception {
        // given
        ProductListResponse response = new ProductListResponse(
                2L, "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null, LocalDateTime.now()
        );

        given(productService.getProducts(eq(ProductType.SPECIAL), any()))
                .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/api/products")
                        .param("type", "SPECIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].name").value("한우 특가"))
                .andExpect(jsonPath("$.data.content[0].type").value("SPECIAL"))
                .andExpect(jsonPath("$.message").value("상품 목록 조회 성공"));
    }

    @Test
    @DisplayName("성공: 상품 상세 조회")
    void 상품_상세_조회_성공() throws Exception {
        // given
        ProductDetailResponse response = new ProductDetailResponse(
                1L, "제주 감귤", ProductType.NORMAL, ProductStatus.ON_SALE,
                15000L, 12000L, null, 100, null, LocalDateTime.now()
        );

        given(productService.getProduct(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("제주 감귤"))
                .andExpect(jsonPath("$.data.type").value("NORMAL"))
                .andExpect(jsonPath("$.data.status").value("ON_SALE"))
                .andExpect(jsonPath("$.message").value("상품 상세 조회 성공"));
    }
}