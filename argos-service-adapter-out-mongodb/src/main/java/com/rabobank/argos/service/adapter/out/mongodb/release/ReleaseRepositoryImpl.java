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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.STARTING_WITH;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseRepositoryImpl implements ReleaseRepository {

    protected static final String ID_FIELD = "_id";
    protected static final String METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD = "metadata.releaseArtifacts.artifactsHash";
    protected static final String METADATA_RELEASE_ARTIFACTS_FIELD = "metadata.releaseArtifacts";
    protected static final String METADATA_SUPPLY_CHAIN_PATH_FIELD = "metadata.supplyChainPath";
    protected static final String COLLECTION_NAME = "fs.files";
    protected static final String RELEASE_ARTIFACTS_FIELD = "releaseArtifacts";
    protected static final String ARTIFACTS_HASH = "artifactsHash";
    protected static final String HASHES = "hashes";
    protected static final String SUPPLY_CHAIN_PATH_FIELD = "supplyChainPath";
    protected static final String RELEASE_DATE_FIELD = "releaseDate";
    protected static final String METADATA_FIELD = "metadata";
    
    private final GridFsTemplate gridFsTemplate;

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper releaseFileJsonMapper;

    @SneakyThrows
    @Override
    public ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier) {
        OffsetDateTime releaseDate = OffsetDateTime.now(ZoneOffset.UTC);
        releaseDossierMetaData.setReleaseDate(releaseDate);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            releaseFileJsonMapper.writeValue(outputStream, releaseDossier);
            try (InputStream inputStream = outputStream.toInputStream()) {
                String fileName = "release-" + releaseDossierMetaData.getSupplyChainPath() + "-" + releaseDate.toInstant().getEpochSecond() + ".json";
                ObjectId objectId = gridFsTemplate.store(inputStream, fileName, "application/json", releaseDossierMetaData);
                releaseDossierMetaData.setDocumentId(objectId.toHexString());
                return releaseDossierMetaData;
            }
        }
    }

    @Override
    public Optional<ReleaseDossierMetaData> findReleaseByReleasedArtifactsAndPath(List<List<String>> releasedArtifacts, String path) {
        checkForEmptyArtifacts(releasedArtifacts);

        Criteria criteria = createArtifactCriteria(releasedArtifacts);

        addOptionalPathCriteria(path, criteria);

        Query query = new Query(criteria);
        log.info("findReleaseByReleasedArtifactsAndPath: {}", query);
        List<ReleaseDossierMetaData> releaseDossierMetaData = mongoTemplate.find(query, ReleaseDossierMetaData.class, COLLECTION_NAME);

        if (releaseDossierMetaData.size() > 1) {
            throw new NotFoundException("no unique release was found please specify a supply chain path parameter");
        } else if (releaseDossierMetaData.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(releaseDossierMetaData.iterator().next());
        }

    }

    private void checkForEmptyArtifacts(List<List<String>> releasedArtifacts) {
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

    private Criteria createArtifactCriteria(List<List<String>> releasedArtifacts) {

        Map<String, List<String>> artifactHashes = ReleaseDossierMetaData.createArtifactsHashMap(releasedArtifacts);
        Criteria criteria = new Criteria();
        List<Criteria> andOperations = new ArrayList<>();
        artifactHashes.forEach((totalHash, hashesList) -> andOperations
                .add(Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD)
                        .elemMatch(Criteria.where(totalHash).is(hashesList))));
        criteria.andOperator(andOperations.toArray(new Criteria[0]));
        return criteria;

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
    public boolean artifactsAreReleased(List<String> releasedArtifacts, String path) {
        String artifactsHash = ReleaseDossierMetaData.createHashFromArtifactList(releasedArtifacts);
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where(METADATA_RELEASE_ARTIFACTS_FIELD).is(artifactsHash));
        addOptionalPathCriteria(path, criteria);
        Query query = new Query(criteria);
        log.info("artifactsAreReleased: {}", query);
        return mongoTemplate.exists(query, COLLECTION_NAME);
    }

}
