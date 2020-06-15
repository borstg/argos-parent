package com.rabobank.argos.service.adapter.out.mongodb.auditlog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
public class AuditlogDatabaseChangeLog {
    @ChangeSet(order = "001", id = "AuditlogDatabaseChangeLog-1", author = "michel")
    public void createAuditCollection(MongoTemplate template) {
        template.createCollection("auditlogs");
    }
}
