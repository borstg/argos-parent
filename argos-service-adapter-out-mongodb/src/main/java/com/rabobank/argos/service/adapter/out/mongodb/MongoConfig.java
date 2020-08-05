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
package com.rabobank.argos.service.adapter.out.mongodb;

import com.github.cloudyrock.mongock.SpringBootMongock;
import com.github.cloudyrock.mongock.SpringBootMongockBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.service.adapter.out.mongodb.release.DateToOffsetTimeConverter;
import com.rabobank.argos.service.adapter.out.mongodb.release.DocumentToReleaseDossierMetaDataConverter;
import com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataToDocumentConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new DateToOffsetTimeConverter());
        converterList.add(new OffsetTimeToDateConverter());
        converterList.add(new ReleaseDossierMetaDataToDocumentConverter());
        converterList.add(new DocumentToReleaseDossierMetaDataConverter());
        return new MongoCustomConversions(converterList);
    }

    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;
    
    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;


    @Bean
    public SpringBootMongock mongogock(MongoTemplate mongoTemplate, ApplicationContext springContext) {
        return new SpringBootMongockBuilder(mongoTemplate, "com.rabobank.argos.service.adapter.out.mongodb")
                .setApplicationContext(springContext)
                .build();
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoURI);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }
    
    @Override
    public Collection<String> getMappingBasePackages() {
        return Arrays.asList("com.rabobank.argos.service.domain");
    }
}
