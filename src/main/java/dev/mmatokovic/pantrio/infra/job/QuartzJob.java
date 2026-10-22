package dev.mmatokovic.pantrio.infra.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class QuartzJob extends QuartzJobBean {

    private final Job generateItemEnrichmentJob;
    private final JobOperator jobOperator;
    private final BatchJobProperties batchJobProperties;
    private final Clock clock;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        var jobName = generateItemEnrichmentJob.getName();
        if (!batchJobProperties.enabled()) {
            log.atInfo().addKeyValue("job", jobName).log("Job is disabled, skipping execution");
            return;
        }

        var runId = clock.millis();
        try {
            var jobParameters = new JobParametersBuilder()
                .addLong("run.id", runId)
                .toJobParameters();
            jobOperator.start(generateItemEnrichmentJob, jobParameters);
            log.atInfo().addKeyValue("job", jobName).addKeyValue("run_id", runId).log("Job started");
        } catch (org.springframework.batch.core.job.JobExecutionException e) {
            log.atError().addKeyValue("job", jobName).addKeyValue("run_id", runId).setCause(e).log("Job start failed");
            throw new JobExecutionException(e);
        }
    }
}
