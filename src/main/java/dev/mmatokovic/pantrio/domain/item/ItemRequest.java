package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    Unit unit,
    @URL
    String pictureUrl,
    @NotNull
    Boolean disabled
) {
    public enum Unit {
        PCS("pcs"),
        G("g"),
        KL("kl"),
        L("l"),
        ML("ml");

        private final String value;

        Unit(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }
}
