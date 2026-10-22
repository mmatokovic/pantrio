package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.job.JobManagementService;
import dev.mmatokovic.pantrio.app.job.JobStateEnum;
import dev.mmatokovic.pantrio.app.product.ProductService;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.domain.validation.Barcode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;
import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.TRIGGER_NAME;

@Validated
@RestController
@Tag(name = "Job")
@RequestMapping(path = "/pull", version = "v1")
@RequiredArgsConstructor
public class JobManagementController {

    private final JobManagementService jobManagementService;
    private final Job generateItemEnrichmentJob;
    private final ProductService productService;

    @GetMapping(value = "/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> pullProduct(@PathVariable @Barcode String barcode) {
        return ResponseEntity.ok(productService.findProductByBarcode(barcode));
    }

    @PostMapping("/job")
    public ResponseEntity<Void> executeJob() {
        jobManagementService.execute(generateItemEnrichmentJob);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/job")
    public ResponseEntity<Void> interruptJob() {
        jobManagementService.interrupt(JOB_NAME);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/job")
    public ResponseEntity<String> rescheduleJob(@RequestParam("cron") String cron) {
        return ResponseEntity.ok(jobManagementService.reschedule(TRIGGER_NAME, cron).toString());
    }

    @PutMapping("/job/state")
    public ResponseEntity<Void> changeJobState(@RequestParam("state") JobStateEnum state) {
        jobManagementService.changeState(JOB_NAME, state);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/job", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getJobStatus() {
        return ResponseEntity.ok(Map.of("state", jobManagementService.getJobStatus(TRIGGER_NAME)));
    }
}
