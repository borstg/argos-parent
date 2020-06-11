package com.rabobank.argos.service.domain.auditlog;

public interface ArgumentBodySerializer {
    <T> String serialize(T arg);
}
