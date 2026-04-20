package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.domain.product.service.PopularSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularSearchResetScheduler {

    private final PopularSearchService popularSearchService;

    // 매일 00시 인기검색어 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetPopularKeywords() {
        popularSearchService.clearTodayKeywords();
        log.info("인기검색어 초기화 완료");
    }
}