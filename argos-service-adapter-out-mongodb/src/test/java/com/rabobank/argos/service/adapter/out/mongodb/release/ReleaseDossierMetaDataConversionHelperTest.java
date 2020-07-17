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
package com.rabobank.argos.service.adapter.out.mongodb.release;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class ReleaseDossierMetaDataConversionHelperTest {

    protected static final String HASH = "71ed24f24e838b18a4bc53aac2638155692b43289ca9778c37139859fc6e619d";
    protected static final List<String> ARTIFACT_LIST = List.of("string", "string2");

    @Test
    void convertToDocumentList() {
        List<Document> documents = ReleaseDossierMetaDataConversionHelper.convertToDocumentList(List.of(Set.of("string", "string2")));
        assertThat(documents, hasSize(1));
        Document document = documents.iterator().next();
        assertThat(document.containsKey(HASH), is(true));
        assertThat(document.get(HASH), is(ARTIFACT_LIST));
    }

}