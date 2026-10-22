package dev.mmatokovic.pantrio.app.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobManagementServiceImplShould {

    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job job;

    @Mock
    private JobExecution jobExecution;

    @Test
    void execute_startJobWithClockBasedRunId() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));
        var jobParameters = new JobParametersBuilder().addLong("run.id", NOW.toEpochMilli()).toJobParameters();
        when(jobOperator.start(job, jobParameters)).thenReturn(jobExecution);

        jobManagementService.execute(job);

        verifyNoMoreInteractions(jobOperator);
        verifyNoInteractions(scheduler, job, jobExecution);
    }

    @Test
    void interrupt_interruptQuartzJob() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(scheduler.interrupt(new JobKey("itemEnrichmentJob"))).thenReturn(true);

        jobManagementService.interrupt("itemEnrichmentJob");

        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator);
    }

    @Test
    void reschedule_returnNextFireTimeInClockZone() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));
        var triggerKey = TriggerKey.triggerKey("itemEnrichmentTrigger");
        Trigger existingTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
            .build();
        Trigger rescheduledTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).build();
        when(scheduler.getTrigger(triggerKey)).thenReturn(existingTrigger);
        when(scheduler.rescheduleJob(triggerKey, rescheduledTrigger)).thenReturn(Date.from(NOW));

        var nextFireTime = jobManagementService.reschedule("itemEnrichmentJob", "itemEnrichmentTrigger", "0 0 12 * * ?");

        assertThat(nextFireTime).isEqualTo(LocalDateTime.of(2026, 10, 22, 10, 0));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator);
    }

    @Test
    void changeState_resumeJob_whenStateIsResume() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));

        jobManagementService.changeState("itemEnrichmentJob", "resume");

        verify(scheduler).resumeJob(new JobKey("itemEnrichmentJob"));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator);
    }

    @Test
    void changeState_pauseJob_whenStateIsPause() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));

        jobManagementService.changeState("itemEnrichmentJob", "PAUSE");

        verify(scheduler).pauseJob(new JobKey("itemEnrichmentJob"));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator);
    }

    @Test
    void changeState_throwIllegalArgument_whenStateIsUnknown() {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> jobManagementService.changeState("itemEnrichmentJob", "STOP"))
            .withMessage("No valid job state given! Use 'RESUME' or 'PAUSE'.");
        verifyNoInteractions(scheduler, jobOperator);
    }

    @Test
    void getJobStatus_returnTriggerStateName() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, jobOperator, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(scheduler.getTriggerState(TriggerKey.triggerKey("itemEnrichmentTrigger"))).thenReturn(Trigger.TriggerState.PAUSED);

        var status = jobManagementService.getJobStatus("itemEnrichmentJob", "itemEnrichmentTrigger");

        assertThat(status).isEqualTo("PAUSED");
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator);
    }
}
