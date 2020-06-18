package com.rabobank.argos.service.adapter.in.rest.link;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogSignatureArgumentFilterTest {

    @Mock
    private RestLinkMetaBlock restLinkMetaBlock;

    @Mock
    private RestSignature restSignature;

    private AuditLogSignatureArgumentFilter auditLogSignatureArgumentFilter;

    @BeforeEach
    void setup() {
        auditLogSignatureArgumentFilter = new AuditLogSignatureArgumentFilter();

    }

    @Test
    void filterObjectArguments() {
        when(restLinkMetaBlock.getSignature()).thenReturn(restSignature);
        Map<String, Object> objectArguments = auditLogSignatureArgumentFilter.filterObjectArguments(restLinkMetaBlock);
        assertThat(objectArguments.containsKey("signature"), is(true));
        assertThat(objectArguments.get("signature"), sameInstance((restSignature)));
    }
}