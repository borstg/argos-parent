package com.rabobank.argos.service.adapter.in.rest.link;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.domain.auditlog.AuditParam;
import com.rabobank.argos.service.domain.auditlog.ObjectArgumentFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("auditLogSignatureArgumentFilter")
public class AuditLogSignatureArgumentFilter implements ObjectArgumentFilter<RestLinkMetaBlock> {

    @Override
    public Map<String, Object> filterObjectArguments(RestLinkMetaBlock argumentValue, AuditParam auditParam) {
        Map<String, Object> argumentValues = new HashMap<>();
        argumentValues.put(auditParam.value(), argumentValue.getSignature());
        return argumentValues;
    }
}
