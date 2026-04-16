package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TimeSaleScheduleService {

    private final Scheduler scheduler;

    public void scheduleStartJob(Long productId, LocalDateTime saleStartTime) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(TimeSaleStartJob.class)
                    .withIdentity("timesale-start-" + productId) // 상품별 시작 Job 이름
                    .usingJobData("productId", productId)        // Job 실행 시 사용할 상품 ID
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("timesale-start-trigger-" + productId)
                    .startAt(Date.from(saleStartTime.atZone(ZoneId.systemDefault()).toInstant())) // 시작 시각에 실행
                    .build();

            scheduler.scheduleJob(jobDetail, trigger); // 시작 Job 예약
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // Quartz 예약 실패 시 공통 예외 처리
        }
    }

    public void scheduleEndJob(Long productId, LocalDateTime saleEndTime) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(TimeSaleEndJob.class)
                    .withIdentity("timesale-end-" + productId) // 상품별 종료 Job 이름
                    .usingJobData("productId", productId)      // Job 실행 시 사용할 상품 ID
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("timesale-end-trigger-" + productId)
                    .startAt(Date.from(saleEndTime.atZone(ZoneId.systemDefault()).toInstant())) // 종료 시각에 실행
                    .build();

            scheduler.scheduleJob(jobDetail, trigger); // 종료 Job 예약
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // Quartz 예약 실패 시 공통 예외 처리
        }
    }
}