package dev.mmatokovic.pantrio.infra.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom Jackson deserializer for {@link LocalDateTime} that accepts both zoned datetime strings
 * (e.g. "2026-06-15T09:43:26+02:00") and plain ISO datetime strings (e.g. "2026-06-15T09:43:26").
 * The timezone component is discarded, keeping only the local date and time part.
 */
public class TimestampDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    protected TimestampDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) {
        String dateString = jp.readValueAs(String.class);
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            // workaround for integration tests
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
