package com.spartafarmer.agri_commerce.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    @Configuration
    static class PageableConfig {

        @org.springframework.context.annotation.Bean
        public PageableHandlerMethodArgumentResolverCustomizer customizePageable() {
            return resolver -> resolver.setMaxPageSize(100); // 페이지 최대 크기 전역 제한
        }
    }
}