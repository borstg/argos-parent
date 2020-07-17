package com.rabobank.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JacksonOffsetDateTimeMapperTest {


    JacksonOffsetDateTimeMapper jacksonOffsetDateTimeMapper;

    @BeforeEach
    void setUp() {
        jacksonOffsetDateTimeMapper = new JacksonOffsetDateTimeMapper();
    }

    @SneakyThrows
    @Test
    void objectMapper() {
        ObjectMapper objectMapper = jacksonOffsetDateTimeMapper.objectMapper();
        String dateTime = "2020-07-30T18:35:24Z";
        DateTimeObject dateTimeObject = new DateTimeObject();
        dateTimeObject.setOffsetDateTime(OffsetDateTime.parse(dateTime));
        String jsonString = objectMapper.writeValueAsString(dateTimeObject);
        assertThat(jsonString, is("{\"offsetDateTime\":\"2020-07-30T18:35:24Z\"}"));
    }

    @Data
    static class DateTimeObject {
        private OffsetDateTime offsetDateTime;
    }
}