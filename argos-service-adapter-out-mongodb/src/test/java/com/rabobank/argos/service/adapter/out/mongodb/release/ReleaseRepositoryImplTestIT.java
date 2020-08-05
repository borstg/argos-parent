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
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.service.adapter.out.mongodb.OffsetTimeToDateConverter;
import com.rabobank.argos.service.domain.release.ReleaseRepository;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReleaseRepositoryImplTestIT {
    protected static final List<List<String>> RELEASE_ARTIFACTS = List.of(List.of("hash1-1", "hash1-2"), List.of("hash2-1", "hash2-2"));
    private MongodExecutable mongodExecutable;
    
    @Autowired
    private ReleaseRepository releaseRepository;
    
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @BeforeAll 
    void setup() throws IOException { 
        String ip = "localhost"; 
        int port = Network.getFreeServerPort(); 
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        MongodStarter starter = MongodStarter.getDefaultInstance(); 
        mongodExecutable = starter.prepare(mongodConfig); 
        mongodExecutable.start(); 
        String connectionString = "mongodb://localhost:" + port; 
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        MongoDbFactory factory = new SimpleMongoClientDbFactory(MongoClients.create(connectionString), "test");
        gridFsTemplate = new GridFsTemplate(factory, getDefaultMongoConverter(factory)); 
        ObjectMapper mapper = new ObjectMapper();
        releaseRepository = new ReleaseRepositoryImpl(gridFsTemplate, mongoTemplate, mapper); }

    @AfterAll
    void clean() {
        mongodExecutable.stop();
    }

    private static MongoConverter getDefaultMongoConverter(MongoDbFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(List.of(new DateToOffsetTimeConverter(), new OffsetTimeToDateConverter()));
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.afterPropertiesSet();
        return converter;
    }

    @Test
    void storeReleaseAndRetreival() {
        ReleaseDossierMetaData stored = storeReleaseDossier();
        assertThat(stored.getDocumentId(), is(IsNull.notNullValue()));
        assertThat(stored.getReleaseDate(), is(IsNull.notNullValue()));
        Optional<String> storedFile = releaseRepository.getRawReleaseFileById(stored.getDocumentId());
        assertThat(storedFile.isEmpty(), is(false));
    }

    private ReleaseDossierMetaData storeReleaseDossier() {
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder().supplyChainId("supplychain").build();
        ReleaseDossier releaseDossier = ReleaseDossier.builder().layoutMetaBlock(layoutMetaBlock).build();
        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(RELEASE_ARTIFACTS)
                .supplyChainPath("path.to.supplychain").build();

        return releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
    }


    @Test
    void findReleaseByReleasedArtifactsAndPath() {
        storeReleaseDossier();
        Optional<ReleaseDossierMetaData> dossierMetaData = releaseRepository
                .findReleaseByReleasedArtifactsAndPath(RELEASE_ARTIFACTS, "path.to");
        assertThat(dossierMetaData.isPresent(), is(true));

        Optional<ReleaseDossierMetaData> emptyDossier = releaseRepository
                .findReleaseByReleasedArtifactsAndPath(List.of(List.of("hash1-incorrect", "hash1-2"), List.of("hash2-1", "hash2-2")), null);

        assertThat(emptyDossier.isPresent(), is(false));
    }

    @Test
    void artifactsAreReleased() {
        storeReleaseDossier();
        boolean artifactsAreReleased = releaseRepository
                .artifactsAreReleased(RELEASE_ARTIFACTS.get(0), "path.to");
        assertThat(artifactsAreReleased, is(true));
    }


    @AfterEach
    void removeData() {
        gridFsTemplate.delete(new Query());
    }


}