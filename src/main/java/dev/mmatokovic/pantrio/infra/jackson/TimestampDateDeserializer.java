package dev.mmatokovic.pantrio.infra.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom Jackson deserializer for {@link LocalDate} that accepts both zoned datetime strings
 * (e.g. "2026-06-15T00:00:00+02:00") and plain ISO date strings (e.g. "2026-06-15").
 * The time and timezone components are discarded, keeping only the date part.
 */
public class TimestampDateDeserializer extends StdDeserializer<LocalDate> {

    protected TimestampDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) {
        String dateString = jp.readValueAs(String.class);
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
            return zonedDateTime.toLocalDate();
        } catch (Exception e) {
            // workaround for integration tests
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        }
    }
}


