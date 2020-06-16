package com.rabobank.argos.service.domain.auditlog;

import java.util.Map;

public interface ObjectArgumentFilter<T> {
    Map<String, String> filterObjectArguments(T argumentValue, ArgumentSerializer argumentSerializer, AuditParam auditParam);
}
