package com.rabobank.argos.service.adapter.out.mongodb.release;

import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.release.ReleaseFile;
import com.rabobank.argos.domain.release.ReleaseFileMetaData;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReleaseRepositoryImplTestIT {
    private MongodExecutable mongodExecutable;
    private ReleaseRepositoryImpl releaseRepository;

    @BeforeAll
    void setup() throws IOException {
        String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        String connectionString = "mongodb://localhost:" + port;
        //MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        MongoDbFactory factory = new SimpleMongoClientDbFactory(MongoClients.create(connectionString), "test");
        GridFsTemplate gridFsTemplate = new GridFsTemplate(factory, getDefaultMongoConverter(factory));
        releaseRepository = new ReleaseRepositoryImpl(gridFsTemplate);
        //SpringMongock runner = new SpringMongockBuilder(mongoTemplate, "com.rabobank.argos.service.adapter.out.mongodb.link").build();
    }


    private static MongoConverter getDefaultMongoConverter(MongoDbFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());
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
    void storeRelease() {
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder().supplyChainId("supplychain").build();
        ReleaseFile releaseFile = ReleaseFile.builder().layoutMetaBlock(layoutMetaBlock).build();
        ReleaseFileMetaData releaseFileMetaData = ReleaseFileMetaData.builder().releaseArtifacts(List.of(Set.of("hash"))).build();
        releaseRepository.storeRelease(releaseFileMetaData, releaseFile);
    }
}