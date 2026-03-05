package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UnitEnum {
    PCS("pcs"),
    G("g"),
    KL("kl"),
    L("l"),
    ML("ml");

    private final String value;

    UnitEnum(String value) {
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
