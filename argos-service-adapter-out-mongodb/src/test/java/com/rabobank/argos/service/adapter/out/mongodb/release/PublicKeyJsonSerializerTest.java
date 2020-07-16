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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rabobank.argos.domain.layout.PublicKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicKeyJsonSerializerTest {
    @Mock
    private PublicKey publicKey;
    @Mock
    private java.security.PublicKey pubKey;
    @Mock
    private JsonGenerator jsonGenerator;
    @Mock
    private SerializerProvider serializerProvider;

    private PublicKeyJsonSerializer publicKeyJsonSerializer;

    @BeforeEach
    void setUp() {
        publicKeyJsonSerializer = new PublicKeyJsonSerializer(PublicKey.class);
    }

    @SneakyThrows
    @Test
    void serialize() {
        when(publicKey.getId()).thenReturn("keyId");
        when(publicKey.getKey()).thenReturn(pubKey);
        when(pubKey.getEncoded()).thenReturn(new byte[0]);
        publicKeyJsonSerializer.serialize(publicKey, jsonGenerator, serializerProvider);
        verify(jsonGenerator).writeStartObject();
        verify(jsonGenerator).writeStringField("id", "keyId");
        verify(jsonGenerator).writeBinaryField("key", new byte[0]);
        verify(jsonGenerator).writeEndObject();
    }
}