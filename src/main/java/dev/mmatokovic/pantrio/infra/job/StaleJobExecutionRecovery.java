package dev.mmatokovic.pantrio.infra.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
class StaleJobExecutionRecovery {

    private final JobRepository jobRepository;
    private final JobOperator jobOperator;

    @EventListener(ApplicationReadyEvent.class)
    void recoverStaleExecutions() {
        for (var jobExecution : jobRepository.findRunningJobExecutions(JOB_NAME)) {
            jobOperator.recover(jobExecution);
            log.atWarn().addKeyValue("job", JOB_NAME).addKeyValue("job_execution_id", jobExecution.getId())
                .log("Recovered job execution left running by a previous shutdown");
        }
    }
}
