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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static com.rabobank.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl.COLLECTION;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountDatabaseChangelogTest {

    @Mock
    private MongoTemplate template;

    @Mock
    private IndexOperations indexOperations;

    @Test
    void addIndex() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        new PersonalAccountDatabaseChangelog().addIndex(template);
        verify(template, times(2)).indexOps(COLLECTION);
    }

    @Test
    void addActiveKeyIndex() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        new PersonalAccountDatabaseChangelog().addActiveKeyIndex(template);
        verify(template, times(1)).indexOps(COLLECTION);
    }

    @Test
    void addIndexToName() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        new PersonalAccountDatabaseChangelog().addIndexToName(template);
        verify(template, times(1)).indexOps(COLLECTION);
    }

    @Test
    void addIndexToLocalPermissionLabelId() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        new PersonalAccountDatabaseChangelog().addIndexToLocalPermissionLabelId(template);
        verify(template, times(1)).indexOps(COLLECTION);
    }

    @Test
    void addInActiveKeyIndex() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        new PersonalAccountDatabaseChangelog().addInActiveKeyIndex(template);
        verify(template, times(1)).indexOps(COLLECTION);
    }
}