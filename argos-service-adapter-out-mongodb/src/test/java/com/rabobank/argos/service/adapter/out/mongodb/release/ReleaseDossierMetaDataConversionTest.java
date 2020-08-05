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

import com.rabobank.argos.domain.release.ReleaseDossierMetaData;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class ReleaseDossierMetaDataConversionTest {

    protected static final List<String> ARTIFACT_LIST = List.of("string", "string2");

    @Test
    void convertToDocumentList() {
        ReleaseDossierMetaData metadata = ReleaseDossierMetaData.builder()
                .releaseArtifacts(List.of(List.of("string", "string2")))
                .documentId("documentId")
                .supplyChainPath("foo.bar")
                .build();
        ReleaseDossierMetaDataToDocumentConverter converter = new ReleaseDossierMetaDataToDocumentConverter();
        Document document = converter.convert(metadata);
        assertThat(document.get("supplyChainPath"), is("foo.bar"));
        Document releaseArtifacts1 = ((List<Document>)document.get("releaseArtifacts")).iterator().next();
        assertThat(releaseArtifacts1.containsKey("artifactsHash"), is(true));
        assertThat(releaseArtifacts1.get("artifactsHash"), is("71ed24f24e838b18a4bc53aac2638155692b43289ca9778c37139859fc6e619d"));
    }

}