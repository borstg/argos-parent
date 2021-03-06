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
package com.rabobank.argos.service.adapter.out.mongodb.link;

import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.COLLECTION;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkMetaBlockRepositoryImplTest {


    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SHA = "sha";

    @Mock
    private MongoTemplate template;

    @Mock
    private LinkMetaBlock link;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private LinkMetaBlockRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LinkMetaBlockRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainId() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndSha() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSha(SUPPLY_CHAIN_ID, SHA);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"$or\" : [{ \"link.materials.hash\" : \"sha\"}, { \"link.products.hash\" : \"sha\"}]}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHashes() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLY_CHAIN_ID, "layoutSegmentName", "stepName", singletonList(SHA));
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"link.layoutSegmentName\" : \"layoutSegmentName\"}, { \"link.stepName\" : \"stepName\"}, { \"link.products.hash\" : \"sha\"}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndStepNameAndMaterialHash() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLY_CHAIN_ID, "layoutSegmentName", "stepName", singletonList(SHA));
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"link.layoutSegmentName\" : \"layoutSegmentName\"}, { \"link.stepName\" : \"stepName\"}, { \"link.materials.hash\" : \"sha\"}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes() {
        new Artifact("file1", SHA);
        EnumMap<ArtifactType, Set<Artifact>> artifactMap = new EnumMap<>(ArtifactType.class);
        artifactMap.put(ArtifactType.MATERIALS, new HashSet<>());
        artifactMap.get(ArtifactType.MATERIALS).add(new Artifact("file1", SHA));
        
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(SUPPLY_CHAIN_ID, "layoutSegmentName", "stepName", artifactMap);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"link.layoutSegmentName\" : \"layoutSegmentName\"}, { \"link.stepName\" : \"stepName\"}, { \"link.materials.hash\" : \"sha\", \"link.materials.uri\" : \"file1\"}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByRunId() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findByRunId(SUPPLY_CHAIN_ID, "layoutSegmentName", "runId", singleton("resolvedStep"));
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"link.runId\" : \"runId\", \"link.layoutSegmentName\" : \"layoutSegmentName\", \"link.stepName\" : { \"$nin\" : [\"resolvedStep\"]}}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteBySupplyChainId() {
        repository.deleteBySupplyChainId("supplyChainId");
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(link);
        verify(template).save(link, COLLECTION);
    }
}
