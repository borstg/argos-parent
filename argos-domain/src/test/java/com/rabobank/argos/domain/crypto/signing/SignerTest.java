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

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.crypto.KeyPair;
import com.rabobank.argos.domain.crypto.PublicKeyFactory;
import com.rabobank.argos.domain.crypto.ServiceAccountKeyPair;
import com.rabobank.argos.domain.crypto.Signature;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignerTest {

    private static final char[] PASSWORD = "password".toCharArray();
    private static final char[] OTHER_PASSWORD = "other password".toCharArray();

    private ServiceAccountKeyPair pair1;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException, OperatorCreationException {
    	KeyPair keyPair = KeyPair.createKeyPair(PASSWORD);
    	pair1 = new ServiceAccountKeyPair(keyPair.getKeyId(), keyPair.getPublicKey(), keyPair.getEncryptedPrivateKey(), null);
    	publicKey = PublicKeyFactory.instance(pair1.getPublicKey());
    }

    @Test
    void sign() throws DecoderException, GeneralSecurityException {
        Signature signature = Signer.sign(pair1, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(pair1.getKeyId()));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA384withECDSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));
    }
    
    @Test
    void signInvalidPassword() throws DecoderException, GeneralSecurityException {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		Signer.sign(pair1, OTHER_PASSWORD, "string to sign");
          });
    	assertEquals("unable to read encrypted data: Error finalising cipher", exception.getMessage());
    }
    
}
