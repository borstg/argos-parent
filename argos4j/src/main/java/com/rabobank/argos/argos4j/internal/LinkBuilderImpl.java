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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.LinkBuilder;
import com.rabobank.argos.argos4j.LinkBuilderSettings;
import com.rabobank.argos.argos4j.internal.mapper.RestMapper;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.crypto.ServiceAccountKeyPair;
import com.rabobank.argos.domain.crypto.Signature;
import com.rabobank.argos.domain.crypto.signing.JsonSigningSerializer;
import com.rabobank.argos.domain.crypto.signing.Signer;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.RequiredArgsConstructor;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

import org.mapstruct.factory.Mappers;

@RequiredArgsConstructor
public class LinkBuilderImpl implements LinkBuilder {

    private final Argos4jSettings settings;
    private final LinkBuilderSettings linkBuilderSettings;

    private ArrayList<Artifact> materials = new ArrayList<>();
    private ArrayList<Artifact> products = new ArrayList<>();

    @Override
    public Argos4jSettings getSettings() {
        return settings;
    }

    public void collectMaterials(FileCollector collector) {
        materials.addAll(ArtifactCollectorFactory.build(collector).collect());
    }

    public void collectProducts(FileCollector collector) {
        products.addAll(ArtifactCollectorFactory.build(collector).collect());
    }

    public void store(char[] signingKeyPassphrase) {
        Link link = Link.builder().runId(linkBuilderSettings.getRunId())
                .materials(materials)
                .products(products)
                .layoutSegmentName(linkBuilderSettings.getLayoutSegmentName())
                .stepName(linkBuilderSettings.getStepName()).build();
        ArgosServiceClient argosServiceClient = new ArgosServiceClient(settings, signingKeyPassphrase);
        ServiceAccountKeyPair keyPair = Mappers.getMapper(RestMapper.class).convertFromRestServiceAccountKeyPair(argosServiceClient.getKeyPair());
        Signature signature;
		try {
			signature = Signer.sign(keyPair, signingKeyPassphrase, new JsonSigningSerializer().serialize(link));
		} catch (GeneralSecurityException | ArgosError e) {
			throw new Argos4jError("The Link object couldn't be signed: "+ e.getMessage());
		}

        argosServiceClient.uploadLinkMetaBlockToService(LinkMetaBlock.builder().link(link).signature(signature).build());
    }
}
