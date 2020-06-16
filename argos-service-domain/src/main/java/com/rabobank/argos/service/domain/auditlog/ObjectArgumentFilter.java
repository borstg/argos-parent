package com.rabobank.argos.service.domain.auditlog;

import java.util.Map;

public interface ObjectArgumentFilter<T> {
    Map<String, Object> filterObjectArguments(T argumentValue, AuditParam auditParam);
}
