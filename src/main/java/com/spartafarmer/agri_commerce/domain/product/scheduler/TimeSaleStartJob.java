package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


@Slf4j
@RequiredArgsConstructor
public class TimeSaleStartJob implements Job {

    private final TimeSaleService timeSaleService; // SpringBeanJobFactory가 주입

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long productId = context.getMergedJobDataMap().getLong("productId"); // JobDataMap에서는 productId만 꺼냄

        try {
            timeSaleService.startProductSale(productId); // 실제 DB 상태 변경은 서비스에서 처리
        } catch (Exception e) {
            log.error("타임세일 시작 처리 실패. productId={}", productId, e); // 실패 로그 남김
            throw new JobExecutionException(e); // Quartz가 실패를 인지하도록 예외 전달
        }
    }
}