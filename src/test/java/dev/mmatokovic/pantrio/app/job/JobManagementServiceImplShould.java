package dev.mmatokovic.pantrio.app.job;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.InvalidRequestException;
import dev.mmatokovic.pantrio.infra.exception.JobManagementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
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
    private JobRepository jobRepository;

    @Mock
    private Job job;

    @Mock
    private JobExecution jobExecution;

    @Test
    void execute_startJobWithClockBasedRunId() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        var jobParameters = new JobParametersBuilder().addLong("run.id", NOW.toEpochMilli()).toJobParameters();
        when(job.getName()).thenReturn("itemEnrichmentJob");
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of());
        when(jobOperator.start(job, jobParameters)).thenReturn(jobExecution);

        jobManagementService.execute();

        verifyNoMoreInteractions(jobOperator, jobRepository, job);
        verifyNoInteractions(scheduler, jobExecution);
    }

    @Test
    void execute_throwConflict_whenAnExecutionIsAlreadyRunning() {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(job.getName()).thenReturn("itemEnrichmentJob");
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of(jobExecution));

        assertThatExceptionOfType(ConflictException.class)
            .isThrownBy(() -> jobManagementService.execute())
            .withMessage("Job itemEnrichmentJob is already running");
        verifyNoMoreInteractions(jobRepository, job);
        verifyNoInteractions(scheduler, jobOperator, jobExecution);
    }

    @Test
    void execute_throwJobManagementException_whenJobIsAlreadyRunning() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        var jobParameters = new JobParametersBuilder().addLong("run.id", NOW.toEpochMilli()).toJobParameters();
        when(job.getName()).thenReturn("itemEnrichmentJob");
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of());
        when(jobOperator.start(job, jobParameters)).thenThrow(new JobExecutionAlreadyRunningException("running"));

        assertThatExceptionOfType(JobManagementException.class)
            .isThrownBy(() -> jobManagementService.execute())
            .withMessage("Job itemEnrichmentJob could not be started");
        verifyNoMoreInteractions(jobOperator, jobRepository, job);
        verifyNoInteractions(scheduler);
    }

    @Test
    void interrupt_stopRunningExecution() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of(jobExecution));
        when(jobOperator.stop(jobExecution)).thenReturn(true);

        jobManagementService.interrupt("itemEnrichmentJob");

        verifyNoMoreInteractions(jobRepository, jobOperator);
        verifyNoInteractions(scheduler, jobExecution);
    }

    @Test
    void interrupt_ignoreExecution_whenItFinishedBeforeTheStop() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of(jobExecution));
        when(jobOperator.stop(jobExecution)).thenThrow(new JobExecutionNotRunningException("finished"));
        when(jobExecution.getId()).thenReturn(7L);

        jobManagementService.interrupt("itemEnrichmentJob");

        verifyNoMoreInteractions(jobRepository, jobOperator, jobExecution);
        verifyNoInteractions(scheduler);
    }

    @Test
    void interrupt_throwConflict_whenNothingIsRunning() {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of());

        assertThatExceptionOfType(ConflictException.class)
            .isThrownBy(() -> jobManagementService.interrupt("itemEnrichmentJob"))
            .withMessage("Job itemEnrichmentJob is not running");
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(scheduler, jobOperator);
    }

    @Test
    void reschedule_returnNextFireTimeInClockZone() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        var triggerKey = TriggerKey.triggerKey("itemEnrichmentTrigger");
        Trigger existingTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
            .build();
        Trigger rescheduledTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).build();
        when(scheduler.getTrigger(triggerKey)).thenReturn(existingTrigger);
        when(scheduler.rescheduleJob(triggerKey, rescheduledTrigger)).thenReturn(Date.from(NOW));

        var nextFireTime = jobManagementService.reschedule("itemEnrichmentTrigger", "0 0 12 * * ?");

        assertThat(nextFireTime).isEqualTo(LocalDateTime.of(2026, 10, 22, 10, 0));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator, jobRepository);
    }

    @Test
    void reschedule_throwInvalidRequest_whenCronIsInvalid() {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));

        assertThatExceptionOfType(InvalidRequestException.class)
            .isThrownBy(() -> jobManagementService.reschedule("itemEnrichmentTrigger", "every five minutes"))
            .withMessage("Invalid cron expression: every five minutes");
        verifyNoInteractions(scheduler, jobOperator, jobRepository);
    }

    @Test
    void changeState_resumeJob_whenStateIsResume() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));

        jobManagementService.changeState("itemEnrichmentJob", JobStateEnum.RESUME);

        verify(scheduler).resumeJob(new JobKey("itemEnrichmentJob"));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator, jobRepository);
    }

    @Test
    void changeState_pauseJob_whenStateIsPause() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));

        jobManagementService.changeState("itemEnrichmentJob", JobStateEnum.PAUSE);

        verify(scheduler).pauseJob(new JobKey("itemEnrichmentJob"));
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator, jobRepository);
    }

    @Test
    void changeState_throwJobManagementException_whenSchedulerFails() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        doThrow(new SchedulerException("store unavailable")).when(scheduler).pauseJob(new JobKey("itemEnrichmentJob"));

        assertThatExceptionOfType(JobManagementException.class)
            .isThrownBy(() -> jobManagementService.changeState("itemEnrichmentJob", JobStateEnum.PAUSE))
            .withMessage("Job itemEnrichmentJob could not be set to PAUSE");
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator, jobRepository);
    }

    @Test
    void getJobStatus_returnTriggerStateName() throws Exception {
        var jobManagementService = new JobManagementServiceImpl(scheduler, job, jobOperator, jobRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
        when(scheduler.getTriggerState(TriggerKey.triggerKey("itemEnrichmentTrigger"))).thenReturn(Trigger.TriggerState.PAUSED);

        var status = jobManagementService.getJobStatus("itemEnrichmentTrigger");

        assertThat(status).isEqualTo("PAUSED");
        verifyNoMoreInteractions(scheduler);
        verifyNoInteractions(jobOperator, jobRepository);
    }
}
