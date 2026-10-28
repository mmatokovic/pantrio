package dev.mmatokovic.pantrio.infra.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

import java.util.Set;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaleJobExecutionRecoveryShould {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobOperator jobOperator;

    @Mock
    private JobExecution jobExecution;

    @Test
    void recoverStaleExecutions_recoverEveryExecutionLeftRunning() {
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of(jobExecution));
        when(jobOperator.recover(jobExecution)).thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(7L);

        new StaleJobExecutionRecovery(jobRepository, jobOperator).recoverStaleExecutions();

        verifyNoMoreInteractions(jobRepository, jobOperator, jobExecution);
    }

    @Test
    void recoverStaleExecutions_doNothing_whenNothingIsRunning() {
        when(jobRepository.findRunningJobExecutions("itemEnrichmentJob")).thenReturn(Set.of());

        new StaleJobExecutionRecovery(jobRepository, jobOperator).recoverStaleExecutions();

        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(jobOperator, jobExecution);
    }
}
