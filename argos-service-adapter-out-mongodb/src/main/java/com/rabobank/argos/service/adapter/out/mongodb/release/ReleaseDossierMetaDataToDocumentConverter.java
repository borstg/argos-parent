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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;

import com.rabobank.argos.domain.release.ReleaseDossierMetaData;

import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.*;

public class ReleaseDossierMetaDataToDocumentConverter implements Converter<ReleaseDossierMetaData, Document>{
    
    @Override
    public Document convert(ReleaseDossierMetaData releaseDossierMetaData) {
        Document metaData = new Document();
        OffsetDateTime releaseDate = OffsetDateTime.now(ZoneOffset.UTC);
        releaseDossierMetaData.setReleaseDate(releaseDate);
        metaData.put(RELEASE_ARTIFACTS_FIELD, convertReleaseArtifactsToDocumentList(releaseDossierMetaData.getReleaseArtifacts()));
        metaData.put(SUPPLY_CHAIN_PATH_FIELD, releaseDossierMetaData.getSupplyChainPath());
        metaData.put(RELEASE_DATE_FIELD, releaseDate);
        
        return metaData;
    }
    
    private List<Document> convertReleaseArtifactsToDocumentList(List<List<String>> releaseArtifacts) {
        List<Document> documents = new ArrayList<>();
        releaseArtifacts.forEach(l -> {
            Document document = new Document();
            document.put(ARTIFACTS_HASH, ReleaseDossierMetaData.createHashFromArtifactList(l));
            document.put(HASHES, l);
            documents.add(document);
        });
        
        return documents;
    }

}
