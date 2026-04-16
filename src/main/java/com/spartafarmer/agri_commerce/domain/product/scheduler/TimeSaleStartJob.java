package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeSaleStartJob implements Job {

    private final TimeSaleService timeSaleService;

    @Override
    public void execute(JobExecutionContext context) {
        Long productId = context.getMergedJobDataMap().getLong("productId"); // 예약할 때 넣어둔 상품 ID

        try {
            timeSaleService.startProductSale(productId); // 실제 DB 상태 변경은 서비스에서 처리
        } catch (Exception e) {
            log.error("타임세일 시작 처리 실패. productId={}", productId, e); // Quartz Job 실패 로그 남김
            throw e; // 실패를 Quartz에 전달
        }
    }
}