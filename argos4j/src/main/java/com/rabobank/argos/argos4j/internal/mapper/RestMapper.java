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
package com.rabobank.argos.argos4j.internal.mapper;


import com.rabobank.argos.argos4j.rest.api.model.RestArtifact;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestServiceAccountKeyPair;
import com.rabobank.argos.domain.crypto.KeyAlgorithm;
import com.rabobank.argos.domain.crypto.KeyIdProvider;
import com.rabobank.argos.domain.crypto.ServiceAccountKeyPair;
import com.rabobank.argos.domain.crypto.ServiceAccountKeyPair.ServiceAccountKeyPairBuilder;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

@Mapper
public interface RestMapper {

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);
    
    @Mapping(source = "publicKey", target = "publicKey")
    public default ServiceAccountKeyPair convertFromRestServiceAccountKeyPair(RestServiceAccountKeyPair keyPair) {
        if ( keyPair == null ) {
            return null;
        }

        ServiceAccountKeyPairBuilder serviceAccountKeyPairBuilder = ServiceAccountKeyPair.builder();

        byte[] publicKey = keyPair.getPublicKey();
        if ( publicKey != null ) {
        	serviceAccountKeyPairBuilder
        		.keyId(KeyIdProvider.computeKeyId(publicKey))
        		.publicKey( Arrays.copyOf( publicKey, publicKey.length ))
        		.algorithm(KeyAlgorithm.valueOf(keyPair.getAlgorithm().name()));
        }
        
        byte[] encryptedPrivateKey = keyPair.getEncryptedPrivateKey();
        if ( encryptedPrivateKey != null ) {
        	serviceAccountKeyPairBuilder
        		.encryptedPrivateKey( Arrays.copyOf( encryptedPrivateKey, encryptedPrivateKey.length ));
        }

        return serviceAccountKeyPairBuilder.build();
    }

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);

    List<RestArtifact> convertToRestArtifacts(List<Artifact> artifacts);
}
