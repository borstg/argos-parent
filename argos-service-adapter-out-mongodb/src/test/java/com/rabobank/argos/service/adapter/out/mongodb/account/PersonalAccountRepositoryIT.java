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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.github.cloudyrock.mongock.SpringMongock;
import com.github.cloudyrock.mongock.SpringMongockBuilder;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.crypto.KeyPair;
import com.rabobank.argos.service.domain.account.AccountSearchParams;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonalAccountRepositoryIT {


    private static final String PIETJE = "Pietje";
    private static final PersonalAccount PIETJE_ACCOUNT = PersonalAccount.builder().name(PIETJE).email("pietje@piet.nl")
            .activeKeyPair(new KeyPair("keyId1", null, null))
            .inactiveKeyPairs(List.of(
                    new KeyPair("keyId2", null, null),
                    new KeyPair("keyId3", null, null))).build();
    private static final String KLAASJE = "Klaasje";
    private static final PersonalAccount KLAASJE_ACCOUNT = PersonalAccount.builder()
    		.name(KLAASJE).email("klaasje@klaas.nl")
            .activeKeyPair(new KeyPair("keyId4", null, null))
            .inactiveKeyPairs(
            		List.of(new KeyPair("keyId5", null, null),
                    new KeyPair("keyId6", null, null)))
            .build();
    private MongodExecutable mongodExecutable;
    private PersonalAccountRepository personalAccountRepository;

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
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        personalAccountRepository = new PersonalAccountRepositoryImpl(mongoTemplate);
        SpringMongock runner = new SpringMongockBuilder(mongoTemplate, "com.rabobank.argos.service.adapter.out.mongodb.account").build();
        runner.execute();
        loadData();
    }

    private void loadData() {
        personalAccountRepository.save(PIETJE_ACCOUNT);
        personalAccountRepository.save(KLAASJE_ACCOUNT);
    }

    @Test
    void searchByName() {
        assertThat(searchByName("tje"), contains(PIETJE));
        assertThat(searchByName("je"), contains(KLAASJE, PIETJE));
        assertThat(searchByName("J"), contains(KLAASJE, PIETJE));
        assertThat(searchByName("Klaa"), contains(KLAASJE));
        assertThat(searchByName("klaasje"), contains(KLAASJE));
        assertThat(searchByName("Pietje"), contains(PIETJE));
        assertThat(searchByName("z"), empty());
    }

    @Test
    void searchByActiveIds() {
        assertThat(searchActiveIds(List.of("keyId4", "keyId1")), contains(KLAASJE, PIETJE));
        assertThat(searchActiveIds(List.of("keyId4", "otherKey")), contains(KLAASJE));
        assertThat(searchActiveIds(List.of("otherKey", "keyId1")), contains(PIETJE));
        assertThat(searchActiveIds(List.of("otherKey", "keyId2")), empty());
    }

    @Test
    void searchByInActiveIds() {
        assertThat(searchInActiveIds(List.of("keyId1", "keyId2", "keyId3", "keyId4", "keyId5", "keyId6", "other")), contains(KLAASJE, PIETJE));
        assertThat(searchInActiveIds(List.of("keyId6", "otherKey")), contains(KLAASJE));
        assertThat(searchInActiveIds(List.of("otherKey", "keyId2")), contains(PIETJE));
        assertThat(searchInActiveIds(List.of("keyId3")), contains(PIETJE));
        assertThat(searchInActiveIds(List.of("keyId5")), contains(KLAASJE));
        assertThat(searchInActiveIds(List.of("otherKey", "keyId1")), empty());
    }

    private List<String> searchByName(String name) {
        return searchAccount(AccountSearchParams.builder().name(name).build());
    }

    private List<String> searchActiveIds(List<String> activeIds) {
        return searchAccount(AccountSearchParams.builder().activeKeyIds(activeIds).build());
    }

    private List<String> searchInActiveIds(List<String> inActiveIds) {
        return searchAccount(AccountSearchParams.builder().inActiveKeyIds(inActiveIds).build());
    }

    private List<String> searchAccount(AccountSearchParams params) {
        return personalAccountRepository.search(params).stream().map(PersonalAccount::getName).collect(Collectors.toList());
    }

    @AfterAll
    void clean() {
        mongodExecutable.stop();
    }
}
