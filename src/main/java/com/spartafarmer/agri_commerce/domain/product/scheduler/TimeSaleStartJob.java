package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
@RequiredArgsConstructor
public class TimeSaleStartJob implements Job {

    private final TimeSaleService timeSaleService;

    @Override
    public void execute(JobExecutionContext context) {
        Long productId = context.getMergedJobDataMap().getLong("productId"); // 예약할 때 넣어둔 상품 ID

        try {
            timeSaleService.startProductSale(productId); // 실제 DB 상태 변경은 서비스에서 처리
        } catch (Exception e) {
            log.error("타임세일 시작 처리 실패. productId={}", productId, e); // 실패 로그만 남김
            // Quartz에 예외를 다시 던지지 않음
            // 운영 환경에서는 여기서 관리자 알림/실패 이력 적재 확장 가능
        }
    }
}