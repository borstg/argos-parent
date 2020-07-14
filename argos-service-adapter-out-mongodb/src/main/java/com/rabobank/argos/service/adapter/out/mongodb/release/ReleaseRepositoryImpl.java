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
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.service.domain.NotFoundException;
import com.rabobank.argos.service.domain.release.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.STARTING_WITH;

@Component
@RequiredArgsConstructor
public class ReleaseRepositoryImpl implements ReleaseRepository {

    protected static final String ID_FIELD = "_id";
    protected static final String METADATA_RELEASE_ARTIFACTS_FIELD = "metadata.releaseArtifacts";
    protected static final String METADATA_SUPPLY_CHAIN_PATH_FIELD = "metadata.supplyChainPath";
    protected static final String COLLECTION_NAME = "fs.files";
    protected static final String RELEASE_ARTIFACTS_FIELD = "releaseArtifacts";
    protected static final String SUPPLY_CHAIN_PATH_FIELD = "supplyChainPath";
    protected static final String RELEASE_DATE_FIELD = "releaseDate";
    protected static final String METADATA_FIELD = "metadata";
    private final GridFsTemplate gridFsTemplate;

    private final MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("releaseFileJsonMapper")
    private final ObjectMapper releaseFileJsonMapper;


    @SneakyThrows
    @Override
    public ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier) {
        DBObject metaData = new BasicDBObject();
        Date releaseDate = new Date();
        releaseDossierMetaData.setReleaseDate(releaseDate);
        metaData.put(RELEASE_ARTIFACTS_FIELD, releaseDossierMetaData.getReleaseArtifacts());
        metaData.put(SUPPLY_CHAIN_PATH_FIELD, releaseDossierMetaData.getSupplyChainPath());
        metaData.put(RELEASE_DATE_FIELD, releaseDate);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            releaseFileJsonMapper.writeValue(outputStream, releaseDossier);
            try (InputStream inputStream = outputStream.toInputStream()) {
                String fileName = "release-" + releaseDossierMetaData.getSupplyChainPath() + "-" + releaseDate.toInstant().getEpochSecond() + ".json";
                ObjectId objectId = gridFsTemplate.store(inputStream, fileName, "application/json", metaData);
                releaseDossierMetaData.setDocumentId(objectId.toHexString());
                return releaseDossierMetaData;
            }
        }
    }

    @Override
    public Optional<ReleaseDossierMetaData> findReleaseByReleasedArtifactsAndPath(List<Set<String>> releasedArtifacts, String path) {
        if (releasedArtifacts.isEmpty()) {
            throw new ArgosError("releasedArtifacts cannot be empty", ArgosError.Level.WARNING);
        }
        Criteria criteria = createInitialCriteria(releasedArtifacts);

        if (releasedArtifacts.size() > 1) {
            addAdditionalReleaseArtifacts(releasedArtifacts, criteria);
        }

        if (path != null) {
            criteria.and(METADATA_SUPPLY_CHAIN_PATH_FIELD)
                    .regex(Objects.requireNonNull(MongoRegexCreator.INSTANCE
                            .toRegularExpression(path, STARTING_WITH)));
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

    private void addAdditionalReleaseArtifacts(List<Set<String>> releasedArtifacts, Criteria criteria) {
        for (int i = 1; i < releasedArtifacts.size(); i++) {
            List<Criteria> andCriteria = new ArrayList<>();
            andCriteria.add(Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD).elemMatch(new Criteria()
                    .elemMatch(new Criteria()
                            .all(releasedArtifacts.get(i)))));
            criteria.andOperator(andCriteria.toArray(new Criteria[0]));
        }
    }

    private Criteria createInitialCriteria(List<Set<String>> releasedArtifacts) {
        return Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD)
                .elemMatch(new Criteria()
                        .elemMatch(new Criteria()
                                .all(releasedArtifacts.get(0))));
    }

    private static Function<Document, ReleaseDossierMetaData> toMetaDataDossier() {
        return document -> {
            Document metaData = (Document) document.get(METADATA_FIELD);
            List<Set<String>> releaseArtifacts = metaData
                    .getList(RELEASE_ARTIFACTS_FIELD, (Class<List<String>>) (Class<?>) List.class,
                            Collections.emptyList())
                    .stream()
                    .map(HashSet::new)
                    .collect(Collectors.toList());
            return ReleaseDossierMetaData
                    .builder()
                    .documentId(document.getObjectId(ID_FIELD)
                            .toHexString())
                    .releaseArtifacts(releaseArtifacts)
                    .releaseDate(metaData.getDate(RELEASE_DATE_FIELD))
                    .supplyChainPath(metaData.getString(SUPPLY_CHAIN_PATH_FIELD))
                    .build();
        };
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
