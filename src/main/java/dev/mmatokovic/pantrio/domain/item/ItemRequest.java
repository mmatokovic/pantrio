package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ItemRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100)
    String name,
    @Size(max = 500)
    String remarks,
    @Size(max = 13)
    String barcode,
    UnitEnum unit,
    @URL
    @JsonProperty("picture_url")
    String pictureUrl,
    Boolean disabled
) { }
