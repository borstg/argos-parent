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
import com.mongodb.DBObject;
import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.service.domain.NotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataConversionHelper.convertToDocumentList;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.METADATA_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.RELEASE_ARTIFACTS_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.RELEASE_DATE_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.SUPPLY_CHAIN_PATH_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseRepositoryImplTest {
    protected static final String RELEASE_DATE_TIME = "2020-07-30T18:35:24.00Z";
    protected static final String ID = "id";
    protected static final String PATH = "path";
    @Mock
    private GridFsTemplate gridFsTemplate;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ObjectMapper releaseFileJsonMapper;

    @Mock
    private ReleaseDossier releaseDossier;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Mock
    private ObjectId objectId;

    @Mock
    private ReleaseDossierMetaData releaseDossierMetaData;

    @Mock
    private Document document;

    @Mock
    private Document metaData;


    private ReleaseRepositoryImpl releaseRepository;

    @BeforeEach
    void setup() {
        releaseRepository = new ReleaseRepositoryImpl(gridFsTemplate, mongoTemplate, releaseFileJsonMapper);
    }

    @Test
    void storeRelease() {
        when(releaseDossierMetaData.getSupplyChainPath()).thenReturn(PATH);
        when(gridFsTemplate.store(any(InputStream.class), any(String.class), any(String.class), any(DBObject.class)))
                .thenReturn(objectId);
        when(objectId.toHexString()).thenReturn(ID);
        releaseDossierMetaData = releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
        verify(releaseDossierMetaData).setDocumentId(ID);
        verify(releaseDossierMetaData).setReleaseDate(any());
    }

    @Test
    void findReleaseByReleasedArtifactsAndPath() {
        List<List<String>> releasedArtifacts = List.of(List.of("hash1"), List.of("hash2"));
        List<Document> storedReleasedArtifacts = convertToDocumentList(releasedArtifacts);
        when(objectId.toHexString()).thenReturn(ID);
        when(document.get(METADATA_FIELD)).thenReturn(metaData);
        when(metaData.getList(RELEASE_ARTIFACTS_FIELD, Document.class,
                Collections.emptyList())).thenReturn(storedReleasedArtifacts);
        when(document.getObjectId(ID_FIELD)).thenReturn(objectId);
        when(metaData.getDate(RELEASE_DATE_FIELD)).thenReturn(Date.from(Instant.parse(RELEASE_DATE_TIME)));
        when(metaData.getString(SUPPLY_CHAIN_PATH_FIELD)).thenReturn(PATH);
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.singletonList(document));
        Optional<ReleaseDossierMetaData> retrievedReleaseDossierMetaData = releaseRepository
                .findReleaseByReleasedArtifactsAndPath(releasedArtifacts, PATH);
        assertThat(retrievedReleaseDossierMetaData.isEmpty(), is(false));
        assertThat(retrievedReleaseDossierMetaData.get().getDocumentId(), is(ID));
        assertThat(retrievedReleaseDossierMetaData.get().getSupplyChainPath(), is(PATH));
        assertThat(retrievedReleaseDossierMetaData.get().getReleaseArtifacts(), is(releasedArtifacts));
        assertThat(retrievedReleaseDossierMetaData.get().getReleaseDate(), is(OffsetDateTime.parse(RELEASE_DATE_TIME)));
        verify(mongoTemplate).find(queryArgumentCaptor.capture(), any(), any());
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"$and\" : [{ \"metadata.releaseArtifacts\" : { \"$elemMatch\" : { \"e7bf382f6e5915b3f88619b866223ebf1d51c4c5321cccde2e9ff700a3259086\" : [\"hash2\"]}}}, { \"metadata.releaseArtifacts\" : { \"$elemMatch\" : { \"af316ecb91a8ee7ae99210702b2d4758f30cdde3bf61e3d8e787d74681f90a6e\" : [\"hash1\"]}}}], \"metadata.supplyChainPath\" : { \"$regex\" : \"^path\", \"$options\" : \"\"}}, Fields: {}, Sort: {}"));
    }


    @Test
    void findReleaseByReleasedArtifactsAndPathWithMultipleResultsShouldThrowException() {
        List<List<String>> releasedArtifacts = List.of(List.of("hash1"), List.of("hash2"));
        List<Document> storedReleasedArtifacts = convertToDocumentList(releasedArtifacts);
        when(objectId.toHexString()).thenReturn(ID);
        when(document.get(METADATA_FIELD)).thenReturn(metaData);
        when(metaData.getList(RELEASE_ARTIFACTS_FIELD, Document.class,
                Collections.emptyList())).thenReturn(storedReleasedArtifacts);
        when(document.getObjectId(ID_FIELD)).thenReturn(objectId);
        when(metaData.getDate(RELEASE_DATE_FIELD)).thenReturn(Date.from(Instant.parse(RELEASE_DATE_TIME)));
        when(metaData.getString(SUPPLY_CHAIN_PATH_FIELD)).thenReturn(PATH);
        when(mongoTemplate.find(any(), any(), any())).thenReturn(List.of(document, document));
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> releaseRepository
                .findReleaseByReleasedArtifactsAndPath(releasedArtifacts, PATH));
        assertThat(notFoundException.getMessage(), is("no unique release was found please specify a supply chain path parameter"));
    }

    @Test
    void findReleaseByReleasedArtifactsAndPathWithNoResultShouldReturnEmpty() {
        List<List<String>> releasedArtifacts = List.of(List.of("hash1"), List.of("hash2"));
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.emptyList());
        assertThat(releaseRepository.findReleaseByReleasedArtifactsAndPath(releasedArtifacts, PATH).isEmpty(), is(true));
    }

    @Test
    void artifactsAreReleasedShouldReturnTrue() {
        when(mongoTemplate.exists(any(Query.class), any(String.class))).thenReturn(true);
        assertThat(releaseRepository.artifactsAreReleased(List.of("hash1"), PATH), is(true));
    }
}