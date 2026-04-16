package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSaleScheduleService {

    private final Scheduler scheduler;

    public void scheduleJob(Class<? extends Job> jobClass, Long productId, LocalDateTime scheduleTime) {
        try {
            String jobName = jobClass.getSimpleName() + "-" + productId; // Job 클래스명과 상품 ID를 조합한 이름

            if (scheduler.checkExists(JobKey.jobKey(jobName))) {
                log.warn("이미 등록된 Quartz Job입니다. jobName={}, productId={}", jobName, productId);
                return; // 이미 등록된 경우 중복 등록 방지
            }

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("productId", productId.longValue()); // primitive long 값만 저장

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName)
                    .usingJobData(jobDataMap) // JobDataMap에는 productId만 넣음
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + jobName)
                    .startAt(Date.from(scheduleTime.atZone(ZoneId.systemDefault()).toInstant())) // 서버 기본 시간대 기준으로 예약
                    .build();

            scheduler.scheduleJob(jobDetail, trigger); // Quartz Job 예약
        } catch (SchedulerException e) {
            log.error("Quartz 스케줄 등록 실패. productId={}", productId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}