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
package com.rabobank.argos.domain.crypto.signing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.security.PublicKey;

import static java.util.Comparator.comparing;

public class JsonSigningSerializer implements SigningSerializer {

    @Override
    public String serialize(Link link) {
        Link linkClone = Mappers.getMapper(Cloner.class).clone(link);
        linkClone.getMaterials().sort(comparing(Artifact::getUri));
        linkClone.getProducts().sort(comparing(Artifact::getUri));
        return serializeSignable(linkClone);
    }

    @Override
    public String serialize(Layout layout) {
        Layout layoutClone = Mappers.getMapper(Cloner.class).clone(layout);
        layoutClone.getLayoutSegments().sort(comparing(LayoutSegment::getName));
        layoutClone.getLayoutSegments().forEach(layoutSegment -> layoutSegment.getSteps().sort(comparing(Step::getName)));
        return serializeSignable(layoutClone);
    }

    private String serializeSignable(Object signable) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        SimpleModule module = new SimpleModule();
        module.addSerializer(PublicKey.class, new PublicKeySerializer());
        objectMapper.registerModule(module);
        try {
            return objectMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private class PublicKeySerializer extends StdSerializer<PublicKey> {

        public PublicKeySerializer() {
            this(null);
        }

        public PublicKeySerializer(Class<PublicKey> t) {
            super(t);
        }

        @Override
        public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeBinary(value.getEncoded());
        }
    }

}
