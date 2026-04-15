package dev.mmatokovic.pantrio.app.job;

import org.springframework.batch.core.job.Job;

import java.time.LocalDateTime;

public interface JobManagementService {

    void execute(Job job) throws Exception;

    void interrupt(String jobName) throws Exception;

    LocalDateTime reschedule(String jobName, String triggerName, String cron) throws Exception;

    void changeState(String jobName, String newJobState) throws Exception;

    String getJobStatus(String jobName, String triggerName) throws Exception;
}
