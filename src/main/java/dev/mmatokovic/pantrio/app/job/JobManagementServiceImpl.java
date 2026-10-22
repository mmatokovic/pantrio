package dev.mmatokovic.pantrio.app.job;

import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobManagementServiceImpl implements JobManagementService {

    private final Scheduler scheduler;
    private final JobOperator jobOperator;
    private final Clock clock;

    @Override
    public void execute(Job job) throws Exception {
        var jobParams = new JobParametersBuilder()
            .addLong("run.id", clock.millis())
            .toJobParameters();

        jobOperator.start(job, jobParams);
    }

    @Transactional
    @Override
    public void interrupt(String jobName) throws Exception {
        scheduler.interrupt(new JobKey(jobName));
    }

    @Transactional
    @Override
    public LocalDateTime reschedule(String jobName, String triggerName, String cron) throws Exception {
        var triggerKey = TriggerKey.triggerKey(triggerName);
        var existingTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        var newTrigger = existingTrigger.getTriggerBuilder()
            .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
            .build();

        return LocalDateTime.ofInstant(scheduler.rescheduleJob(triggerKey, newTrigger).toInstant(), clock.getZone());
    }

    @Transactional
    @Override
    public void changeState(String jobName, String newJobState) throws Exception {
        if ("RESUME".equalsIgnoreCase(newJobState)) {
            scheduler.resumeJob(new JobKey(jobName));
        } else if ("PAUSE".equalsIgnoreCase(newJobState)) {
            scheduler.pauseJob(new JobKey(jobName));
        } else {
            throw new IllegalArgumentException("No valid job state given! Use 'RESUME' or 'PAUSE'.");
        }
    }

    @Override
    public String getJobStatus(String jobName, String triggerName) throws Exception {
        var triggerKey = TriggerKey.triggerKey(triggerName);
        var triggerState = scheduler.getTriggerState(triggerKey);

        return triggerState.name();
    }
}
