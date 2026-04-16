package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeSaleStartJob implements Job {

    private final TimeSaleService timeSaleService;

    @Override
    public void execute(JobExecutionContext context) {
        Long productId = context.getMergedJobDataMap().getLong("productId"); // 예약할 때 넣어둔 상품 ID
        timeSaleService.startProductSale(productId); // 실제 상태 변경은 트랜잭션 서비스에서 처리
    }
}