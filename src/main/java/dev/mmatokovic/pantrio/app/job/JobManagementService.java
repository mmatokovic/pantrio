package dev.mmatokovic.pantrio.app.job;

import org.springframework.batch.core.job.Job;

import java.time.LocalDateTime;

public interface JobManagementService {

    void execute(Job job);

    void interrupt(String jobName);

    LocalDateTime reschedule(String triggerName, String cron);

    void changeState(String jobName, JobStateEnum jobState);

    String getJobStatus(String triggerName);
}
