package com.rabobank.argos.service.adapter.out.mongodb.release;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.rabobank.argos.domain.layout.PublicKey;

import java.io.IOException;

public class PublicKeySerializer extends StdSerializer<PublicKey> {

    public PublicKeySerializer() {
        this(null);
    }

    public PublicKeySerializer(Class<PublicKey> t) {
        super(t);
    }

    @Override
    public void serialize(PublicKey publicKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", publicKey.getId());
        jsonGenerator.writeBinaryField("key", publicKey.getKey().getEncoded());
        jsonGenerator.writeEndObject();
    }
}
