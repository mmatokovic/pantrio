package dev.mmatokovic.pantrio.domain.item;

import dev.mmatokovic.pantrio.domain.validation.Barcode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ItemRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100)
    String name,
    @Size(max = 500)
    String remarks,
    @Barcode
    String barcode,
    UnitEnum unit,
    @URL
    String pictureUrl,
    Boolean disabled
) { }
