package com.rabobank.argos.service.adapter.in.rest.link;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.domain.auditlog.ArgumentSerializer;
import com.rabobank.argos.service.domain.auditlog.AuditParam;
import com.rabobank.argos.service.domain.auditlog.ObjectArgumentFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("auditLogSignatureArgumentFilter")
public class AuditLogSignatureArgumentFilter implements ObjectArgumentFilter<RestLinkMetaBlock> {

    @Override
    public Map<String, String> filterObjectArguments(RestLinkMetaBlock argumentValue, ArgumentSerializer argumentSerializer, AuditParam auditParam) {
        Map<String, String> argumentValues = new HashMap<>();
        argumentValues.put(auditParam.value(), argumentSerializer.serialize(argumentValue.getSignature()));
        return argumentValues;
    }
}
