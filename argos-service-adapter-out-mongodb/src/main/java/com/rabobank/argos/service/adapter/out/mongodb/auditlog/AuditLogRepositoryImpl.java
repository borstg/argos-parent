/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.adapter.out.mongodb.auditlog;

import com.rabobank.argos.service.domain.auditlog.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.adapter.out.mongodb.auditlog.MongoDBAppender.COLLECTION;

@Component
@Profile("integration-test")
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public String getAuditLogs() {
        List<Document> logs = mongoTemplate.findAll(Document.class, COLLECTION);
        return logs.stream().map(Document::toJson)
                .map(s -> s.replaceAll("\\\\", ""))
                .collect(Collectors.joining(","));
    }
}
