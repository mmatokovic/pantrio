package dev.mmatokovic.pantrio.infra.job;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class QuartzJob extends QuartzJobBean {

    private final Job generateItemEnrichmentJob;
    private final JobOperator jobOperator;
    private final boolean enabled;

    public QuartzJob(
        Job generateItemEnrichmentJob,
        JobOperator jobOperator,
        @Value("${batch.job.enabled}") boolean enabled
    ) {
        this.generateItemEnrichmentJob = generateItemEnrichmentJob;
        this.jobOperator = jobOperator;
        this.enabled = enabled;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        final var jobName = generateItemEnrichmentJob.getName();

        if (!enabled) {
            log.info("Job {} is marked as disabled! Skipping execution...", jobName);
            return;
        }

        long runId = System.currentTimeMillis();

        log.info("Starting job {} with job run ID={}", jobName, runId);

        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("run.id", runId)
                .toJobParameters();

            jobOperator.start(generateItemEnrichmentJob, params);

            log.info("Triggered job {} with job run ID={}", jobName, runId);

        } catch (Exception e) {
            log.error("Exiting job {} failed with job run ID={}", jobName, runId, e);
            throw new JobExecutionException(e);
        }
    }

}
