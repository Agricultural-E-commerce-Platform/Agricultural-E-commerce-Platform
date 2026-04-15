package com.spartafarmer.agri_commerce.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    // JPA EntityManager 주입 (영속성 컨텍스트 관리)
    @PersistenceContext
    private EntityManager em;

    // QueryDSL 동적 쿼리 생성을 위한 JPAQueryFactory Bean 등록
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}