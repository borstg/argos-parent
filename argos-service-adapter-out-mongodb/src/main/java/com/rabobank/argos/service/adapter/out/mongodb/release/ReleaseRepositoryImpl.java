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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.service.domain.NotFoundException;
import com.rabobank.argos.service.domain.release.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReleaseRepositoryImpl implements ReleaseRepository {

    private static final String ID_FIELD = "_id";
    protected static final String METADATA_RELEASE_ARTIFACTS_FIELD = "metadata.releaseArtifacts";
    protected static final String METADATA_SUPPLY_CHAIN_PATH_FIELD = "metadata.supplyChainPath";
    protected static final String COLLECTION_NAME = "fs.files";
    private final GridFsTemplate gridFsTemplate;

    private final MongoTemplate mongoTemplate;

    @Qualifier("releaseFileJsonMapper")
    private final ObjectMapper releaseFileJsonMapper;


    @SneakyThrows
    @Override
    public ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier) {
        DBObject metaData = new BasicDBObject();
        Date releaseDate = new Date();
        releaseDossierMetaData.setReleaseDate(releaseDate);
        metaData.put("releaseArtifacts", releaseDossierMetaData.getReleaseArtifacts());
        metaData.put("supplyChainPath", releaseDossierMetaData.getSupplyChainPath());

        String jsonReleaseFile = releaseFileJsonMapper.writeValueAsString(releaseDossier);

        InputStream inputStream = IOUtils.toInputStream(jsonReleaseFile, StandardCharsets.UTF_8);
        String fileName = "release-" + releaseDossierMetaData.getSupplyChainPath() + "-" + releaseDate.toInstant().getEpochSecond() + ".json";
        ObjectId objectId = gridFsTemplate.store(inputStream, fileName, "application/json", metaData);
        releaseDossierMetaData.setDocumentId(objectId.toHexString());
        return releaseDossierMetaData;
    }

    @Override
    public Optional<ReleaseDossierMetaData> findReleaseByReleasedArtifactsAndPath(List<String> releasedArtifacts, String path) {
        Criteria criteria = Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD)
                .elemMatch(new Criteria()
                        .elemMatch(new Criteria()
                                .all(releasedArtifacts)));
        if (path != null) {
            //criteria.andOperator(Criteria.where(METADATA_SUPPLY_CHAIN_PATH_FIELD).re)
        }

        List<Document> documents = mongoTemplate.find(new Query(criteria), Document.class, COLLECTION_NAME);

        List<ReleaseDossierMetaData> releaseDossierMetaData = documents
                .stream().map(toMetaDataDossier()).collect(Collectors.toList());

        if (releaseDossierMetaData.size() > 1) {
            throw new NotFoundException("no unique release was found please specify a supply chain path parameter");
        } else if (releaseDossierMetaData.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(releaseDossierMetaData.iterator().next());
        }

    }

    private static Function<Document, ReleaseDossierMetaData> toMetaDataDossier() {
        return document -> ReleaseDossierMetaData
                .builder()
                .documentId(document.getObjectId(ID_FIELD)
                        .toHexString())
                .releaseArtifacts(document.getList(METADATA_RELEASE_ARTIFACTS_FIELD,
                        (Class<Set<String>>) (Class<?>) Set.class,
                        Collections.emptyList()))
                .releaseDate(document.getDate("uploadDate"))
                .supplyChainPath(document.getString(METADATA_SUPPLY_CHAIN_PATH_FIELD)).build();
    }

    @SneakyThrows
    @Override
    public Optional<String> getRawReleaseFileById(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where(ID_FIELD).is(id)));
        assert file != null;
        String releaseFileJson = IOUtils.toString(gridFsTemplate.getResource(file).getInputStream(), StandardCharsets.UTF_8.name());
        return Optional.ofNullable(releaseFileJson);
    }

}
