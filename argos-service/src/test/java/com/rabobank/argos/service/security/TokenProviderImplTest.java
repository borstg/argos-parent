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
package com.rabobank.argos.service.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class TokenProviderImplTest {

    public static final String SECRET = "Zrf3tmpRczYszNFS92mFC/JEuxiwhRAe5fO/GdbqL2g9wa2V7bi0VKRuy/VantPuzN/xo43t36zZUGgJNdjD9w==";
    private TokenProviderImpl tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProviderImpl();
        ReflectionTestUtils.setField(tokenProvider, "secret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "timeout", Duration.of(1, ChronoUnit.MINUTES));
        tokenProvider.init();
    }

    @Test
    void createToken() {
        TokenProviderImpl.main(new String[]{});
        String token = tokenProvider.createToken("id");
        assertThat(tokenProvider.validateToken(token), is(true));
        assertThat(tokenProvider.getTokenInfo(token).getAccountId(), is("id"));
        assertThat(tokenProvider.getTokenInfo(token).getSessionId(), hasLength(36));
    }

    @Test
    void validateTokenInCorrectJwt() {
        assertThat(tokenProvider.validateToken("incorrect"), is(false));
    }

    @Test
    void validateTokenWrongKey() {
        String token = tokenProvider.createToken("id");
        ReflectionTestUtils.setField(tokenProvider, "secretKey", Keys.secretKeyFor(SignatureAlgorithm.HS512));
        assertThat(tokenProvider.validateToken(token), is(false));
    }

    @Test
    void validateTokenExpired() {
        ReflectionTestUtils.setField(tokenProvider, "timeout", Duration.of(1, ChronoUnit.NANOS));
        String token = tokenProvider.createToken("id");
        assertThat(tokenProvider.validateToken(token), is(false));
    }
}