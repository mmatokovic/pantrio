package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.product.ProductService;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Product")
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProductResponse> find(@PathVariable String code) {
        return ResponseEntity.ok(service.findProductByBarcode(code));
    }
}
