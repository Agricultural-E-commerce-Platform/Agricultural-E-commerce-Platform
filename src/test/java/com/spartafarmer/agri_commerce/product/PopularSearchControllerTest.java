package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.security.JwtUtil;
import com.spartafarmer.agri_commerce.domain.product.controller.PopularSearchController;
import com.spartafarmer.agri_commerce.domain.product.dto.PopularKeywordResponse;
import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PopularSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("인기검색어 컨트롤러 테스트")
class PopularSearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PopularSearchService popularSearchService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    JwtUtil jwtUtil;

    @Test
    @DisplayName("성공: 인기검색어 조회")
    void 인기검색어_조회_성공() throws Exception {
        // given
        given(popularSearchService.getTopKeywords()).willReturn(List.of(
                new PopularKeywordResponse(1, "딸기", 10L),
                new PopularKeywordResponse(2, "감귤", 8L)
        ));

        // when & then
        mockMvc.perform(get("/api/products/search/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].keyword").value("딸기"))
                .andExpect(jsonPath("$.data[0].searchCount").value(10))
                .andExpect(jsonPath("$.message").value("인기 검색어 조회 성공"));
    }
}