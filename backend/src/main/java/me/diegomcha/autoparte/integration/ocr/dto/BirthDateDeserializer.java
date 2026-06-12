package me.diegomcha.autoparte.integration.ocr.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

class BirthDateDeserializer extends StdScalarDeserializer<LocalDate> {

    protected BirthDateDeserializer() {
        super(String.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                // This will parse two-digit years and assume they are in the range of 100 years before the current date
                .appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.now(ZoneOffset.UTC).minusYears(100))
                .appendPattern("MMdd")
                .toFormatter();

        return formatter.parse(p.getString(), LocalDate::from);
    }
}
