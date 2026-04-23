package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.product.controller.ProductSearchV2Controller;
import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import com.spartafarmer.agri_commerce.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductSearchV2Controller.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("상품 검색 v2 검증 테스트")
class ProductSearchV2ValidationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    PopularSearchService popularSearchService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    JwtUtil jwtUtil;

    @Test
    @DisplayName("실패: 상품 검색 v2 키워드 길이 초과")
    void 상품_검색_V2_실패_키워드길이초과() throws Exception {
        // given
        String longKeyword = "a".repeat(51);

        // when & then
        mockMvc.perform(get("/api/v2/products/search")
                        .param("keyword", longKeyword))
                .andExpect(status().isBadRequest());

        then(productService).should(never()).normalizeKeyword(any());
        then(productService).should(never()).searchProductsWithCache(any(), any());
        then(popularSearchService).should(never()).increaseKeyword(any(), any());
    }
}