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

import java.security.GeneralSecurityException;

import com.rabobank.argos.domain.crypto.HashAlgorithm;
import com.rabobank.argos.domain.crypto.KeyAlgorithm;

public enum SignatureAlgorithm {
	SHA_384_WITH_ECDSA("SHA384withECDSA"), SHA_256_WITH_RSA("SHA256withRSA");;
	
	String value;
	
	SignatureAlgorithm(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
	
	public static SignatureAlgorithm getAlgorithm(KeyAlgorithm keyAlgorithm, HashAlgorithm hashAlgorithm) throws GeneralSecurityException {
    	if (KeyAlgorithm.EC.equals(keyAlgorithm)) {
    		if (HashAlgorithm.SHA384.equals(hashAlgorithm)) {
    			return SHA_384_WITH_ECDSA;
    		} else {
    			throw new GeneralSecurityException(hashAlgorithm+" not supported");
    		}
    	} else {
    		return SHA_256_WITH_RSA;
			//throw new GeneralSecurityException(keyAlgorithm+" not supported");  
    	}
	}

}
