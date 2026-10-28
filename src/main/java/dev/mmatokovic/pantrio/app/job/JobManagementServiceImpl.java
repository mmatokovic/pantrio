package dev.mmatokovic.pantrio.app.job;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.InvalidRequestException;
import dev.mmatokovic.pantrio.infra.exception.JobManagementException;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobManagementServiceImpl implements JobManagementService {

    private final Scheduler scheduler;
    private final Job generateItemEnrichmentJob;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Clock clock;

    @Override
    public void execute() {
        var jobName = generateItemEnrichmentJob.getName();
        if (!jobRepository.findRunningJobExecutions(jobName).isEmpty()) {
            throw new ConflictException("Job " + jobName + " is already running");
        }
        var jobParameters = new JobParametersBuilder()
            .addLong("run.id", clock.millis())
            .toJobParameters();
        try {
            jobOperator.start(generateItemEnrichmentJob, jobParameters);
        } catch (JobExecutionException e) {
            throw new JobManagementException("Job " + jobName + " could not be started", e);
        }
    }

    @Override
    public void interrupt(String jobName) {
        var runningExecutions = jobRepository.findRunningJobExecutions(jobName);
        if (runningExecutions.isEmpty()) {
            throw new ConflictException("Job " + jobName + " is not running");
        }
        for (var jobExecution : runningExecutions) {
            try {
                jobOperator.stop(jobExecution);
            } catch (JobExecutionNotRunningException e) {
                log.atDebug().addKeyValue("job", jobName).addKeyValue("job_execution_id", jobExecution.getId()).log("Job execution already finished");
            }
        }
    }

    @Override
    @Transactional
    public LocalDateTime reschedule(String triggerName, String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new InvalidRequestException("Invalid cron expression: " + cron);
        }
        var triggerKey = TriggerKey.triggerKey(triggerName);
        try {
            if (!(scheduler.getTrigger(triggerKey) instanceof CronTrigger existingTrigger)) {
                throw new NoSuchElementFoundException("Cron trigger " + triggerName + " not found");
            }
            var newTrigger = existingTrigger.getTriggerBuilder()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                .build();
            return LocalDateTime.ofInstant(scheduler.rescheduleJob(triggerKey, newTrigger).toInstant(), clock.getZone());
        } catch (SchedulerException e) {
            throw new JobManagementException("Trigger " + triggerName + " could not be rescheduled", e);
        }
    }

    @Override
    @Transactional
    public void changeState(String jobName, JobStateEnum jobState) {
        var jobKey = new JobKey(jobName);
        try {
            switch (jobState) {
                case RESUME -> scheduler.resumeJob(jobKey);
                case PAUSE -> scheduler.pauseJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new JobManagementException("Job " + jobName + " could not be set to " + jobState, e);
        }
    }

    @Override
    public String getJobStatus(String triggerName) {
        try {
            return scheduler.getTriggerState(TriggerKey.triggerKey(triggerName)).name();
        } catch (SchedulerException e) {
            throw new JobManagementException("State of trigger " + triggerName + " could not be read", e);
        }
    }
}
