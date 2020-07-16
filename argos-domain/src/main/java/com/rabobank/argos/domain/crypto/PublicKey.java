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
package com.rabobank.argos.domain.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublicKey {
    private String keyId;
    private byte[] publicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public PublicKey(String keyId, byte[] publicKey) {
        this.keyId = keyId;
        this.publicKey = publicKey;
    }
    
    @JsonIgnore
    public java.security.PublicKey getJavaPublicKey() throws GeneralSecurityException, IOException {
    	return PublicKeyFactory.instance(publicKey);
    }
}
