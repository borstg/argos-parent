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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataConversionHelper.convertToDocumentList;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataConversionHelper.createHashFromArtifactList;
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
        metaData.put(RELEASE_ARTIFACTS_FIELD, convertToDocumentList(releaseDossierMetaData.getReleaseArtifacts()));
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
        checkForEmptyArtifacts(releasedArtifacts);

        Criteria criteria = createArtifactCriteria(releasedArtifacts);

        addOptionalPathCriteria(path, criteria);

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

    private void checkForEmptyArtifacts(List<Set<String>> releasedArtifacts) {
        if (releasedArtifacts.isEmpty()) {
            throw new ArgosError("releasedArtifacts cannot be empty", ArgosError.Level.WARNING);
        }
    }

    private void addOptionalPathCriteria(String path, Criteria criteria) {
        if (path != null) {
            criteria.and(METADATA_SUPPLY_CHAIN_PATH_FIELD)
                    .regex(Objects.requireNonNull(MongoRegexCreator.INSTANCE
                            .toRegularExpression(path, STARTING_WITH)));
        }
    }

    private Criteria createArtifactCriteria(List<Set<String>> releasedArtifacts) {

        Map<String, List<String>> artifactHashes = createArtifactsHashes(releasedArtifacts);
        Criteria criteria = new Criteria();
        List<Criteria> andOperations = new ArrayList<>();
        artifactHashes.forEach((key, value) -> andOperations
                .add(Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD)
                        .elemMatch(Criteria.where(key).is(value))));
        criteria.andOperator(andOperations.toArray(new Criteria[0]));
        return criteria;

    }

    private Map<String, List<String>> createArtifactsHashes(List<Set<String>> releasedArtifacts) {
        Map<String, List<String>> map = new HashMap<>();
        releasedArtifacts.forEach(artifactSet -> {
            List<String> artifactList = new ArrayList<>(artifactSet);
            Collections.sort(artifactList);
            map.put(createHashFromArtifactList(artifactList), artifactList);
        });
        return map;
    }

    private static Function<Document, ReleaseDossierMetaData> toMetaDataDossier() {
        return document -> {
            Document metaData = (Document) document.get(METADATA_FIELD);
            List<Document> releaseArtifacts = metaData
                    .getList(RELEASE_ARTIFACTS_FIELD, Document.class,
                            Collections.emptyList());
            return ReleaseDossierMetaData
                    .builder()
                    .documentId(document.getObjectId(ID_FIELD)
                            .toHexString())
                    .releaseArtifacts(convertToReleaseArtifacts(releaseArtifacts))
                    .releaseDate(metaData.getDate(RELEASE_DATE_FIELD))
                    .supplyChainPath(metaData.getString(SUPPLY_CHAIN_PATH_FIELD))
                    .build();
        };
    }

    private static List<Set<String>> convertToReleaseArtifacts(List<Document> releaseArtifacts) {
        return releaseArtifacts.stream()
                .flatMap(d -> d.values()
                        .stream()
                        .map(o -> (List<String>) o)
                        .map(HashSet::new)
                ).collect(Collectors.toList());


    }

    @SneakyThrows
    @Override
    public Optional<String> getRawReleaseFileById(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where(ID_FIELD).is(id)));
        assert file != null;
        String releaseFileJson = IOUtils.toString(gridFsTemplate.getResource(file).getInputStream(), StandardCharsets.UTF_8.name());
        return Optional.ofNullable(releaseFileJson);
    }

    @Override
    public boolean artifactsAreReleased(Set<String> releasedArtifacts, String path) {
        List<Set<String>> releasedArtifactsList = new ArrayList(releasedArtifacts);
        checkForEmptyArtifacts(releasedArtifactsList);
        Criteria criteria = createArtifactCriteria(releasedArtifactsList);
        addOptionalPathCriteria(path, criteria);
        return mongoTemplate.exists(new Query(criteria), COLLECTION_NAME);
    }

}
