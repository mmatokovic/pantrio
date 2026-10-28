package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UnitEnum {
    PCS("pcs"),
    G("g"),
    KG("kg"),
    L("l"),
    ML("ml");

    @Getter(onMethod_ = @JsonValue)
    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
