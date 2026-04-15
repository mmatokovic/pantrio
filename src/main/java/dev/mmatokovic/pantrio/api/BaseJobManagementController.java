package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.job.JobManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseJobManagementController {

    private final JobManagementService jobManagementService;
    private final Job batchJob;
    private final String jobName;
    private final String quartzTriggerName;

    @PostMapping("/job")
    public ResponseEntity<Void> executeJob() throws Exception {
        jobManagementService.execute(batchJob);

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/job")
    public ResponseEntity<Void> interruptJob() throws Exception {
        jobManagementService.interrupt(jobName);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/job")
    public ResponseEntity<String> rescheduleJob(@RequestParam("cron") String cron) throws Exception {
        return ResponseEntity.ok(jobManagementService.reschedule(jobName, quartzTriggerName, cron).toString());
    }

    @PutMapping("/job/state")
    public ResponseEntity<Void> changeJobState(@RequestParam("state") String state) throws Exception {
        jobManagementService.changeState(jobName, state);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/job")
    public ResponseEntity<Map<String, String>> getJobStatus() throws Exception {
        String state = jobManagementService.getJobStatus(jobName, quartzTriggerName);
        return ResponseEntity.ok(Map.of("state", state));
    }

}
