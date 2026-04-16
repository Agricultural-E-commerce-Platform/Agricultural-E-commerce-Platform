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

    public void scheduleJob(Class<? extends Job> jobClass, Long productId, LocalDateTime scheduleTime) {
        try {
            String jobName = jobClass.getSimpleName() + "-" + productId; // Job 클래스명과 상품 ID를 조합한 이름

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("productId", productId.longValue()); // Quartz JobDataMap에는 primitive long 값으로 저장

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName)
                    .usingJobData(jobDataMap) // Job 실행 시 사용할 데이터 등록
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + jobName)
                    .startAt(Date.from(scheduleTime.atZone(ZoneId.systemDefault()).toInstant())) // 예약 시각에 실행
                    .build();

            scheduler.scheduleJob(jobDetail, trigger); // Quartz Job 예약
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // Quartz 예약 실패 시 공통 예외 처리
        }
    }
}