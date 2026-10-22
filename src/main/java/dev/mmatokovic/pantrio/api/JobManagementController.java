package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.job.JobManagementService;
import dev.mmatokovic.pantrio.app.product.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.job.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;
import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.TRIGGER_NAME;

@RestController
@Tag(name = "Job")
@RequestMapping("/pull")
public class JobManagementController extends BaseJobManagementController {

    private final ProductService productService;

    public JobManagementController(JobManagementService jobManagementService,
                                   Job generateItemEnrichmentJob,
                                   ProductService productService) {
        super(
            jobManagementService,
            generateItemEnrichmentJob,
            JOB_NAME,
            TRIGGER_NAME
        );
        this.productService = productService;
    }

    @GetMapping("/{barcode}")
    public ResponseEntity<Void> sendDocumentToDms(@PathVariable String barcode) {

        productService.findProductByBarcode(barcode);
        return ResponseEntity.ok().build();
    }
}
