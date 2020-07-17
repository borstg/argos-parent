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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.release.ReleaseDossierMetaData.createHashFromArtifactList;

public class ReleaseDossierMetaDataConversionHelper {
    private ReleaseDossierMetaDataConversionHelper() {
    }

    public static List<Document> convertToDocumentList(List<Set<String>> releaseArtifacts) {
        return releaseArtifacts.stream().map(artifactSet -> {
            List<String> artifactList = new ArrayList<>(artifactSet);
            Collections.sort(artifactList);
            Document document = new Document();
            document.put(createHashFromArtifactList(artifactList), artifactList);
            return document;
        }).collect(Collectors.toList());

    }

}
