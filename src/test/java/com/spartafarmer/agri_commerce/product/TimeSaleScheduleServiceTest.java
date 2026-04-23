package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.scheduler.TimeSaleEndJob;
import com.spartafarmer.agri_commerce.domain.product.scheduler.TimeSaleStartJob;
import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeSaleScheduleServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private TimeSaleScheduleService timeSaleScheduleService;

    @Test
    void Quartz_Job_등록_성공() throws Exception {
        // given
        Long productId = 999L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        when(scheduler.checkExists(JobKey.jobKey("TimeSaleStartJob-" + productId)))
                .thenReturn(false);

        // when
        timeSaleScheduleService.scheduleJob(TimeSaleStartJob.class, productId, scheduleTime);

        // then
        verify(scheduler).checkExists(JobKey.jobKey("TimeSaleStartJob-" + productId));
        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void Quartz_Job_중복등록_방지_성공() throws Exception {
        // given
        Long productId = 998L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        when(scheduler.checkExists(JobKey.jobKey("TimeSaleEndJob-" + productId)))
                .thenReturn(true);

        // when
        timeSaleScheduleService.scheduleJob(TimeSaleEndJob.class, productId, scheduleTime);

        // then
        verify(scheduler).checkExists(JobKey.jobKey("TimeSaleEndJob-" + productId));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void Quartz_Job_등록_실패_스케줄러예외() throws Exception {
        // given
        Long productId = 997L;
        LocalDateTime scheduleTime = LocalDateTime.now().plusSeconds(10);

        when(scheduler.checkExists(JobKey.jobKey("TimeSaleStartJob-" + productId)))
                .thenThrow(new SchedulerException("scheduler error"));

        // when & then
        assertThatThrownBy(() -> timeSaleScheduleService.scheduleJob(TimeSaleStartJob.class, productId, scheduleTime))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}