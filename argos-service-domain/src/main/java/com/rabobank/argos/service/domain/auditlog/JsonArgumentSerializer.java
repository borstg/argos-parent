package com.rabobank.argos.service.domain.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component("JsonArgumentSerializer")
public class JsonArgumentSerializer implements ArgumentBodySerializer {
    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public <T> String serialize(T arg) {
        return objectMapper.writeValueAsString(arg);
    }
}
