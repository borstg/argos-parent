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
package com.rabobank.argos.service.security.oauth2;

import com.rabobank.argos.service.ArgosServiceApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ArgosServiceApplication.class})
class OAuth2ProvidersTestIT {
    @Autowired
    OAuth2Providers oAuth2Providers;

    @Test
    void testParsing() {
        assertThat(oAuth2Providers.getProvider().isEmpty(), is(false));
        assertThat(oAuth2Providers.getProvider().get("azure"), is(notNullValue()));
        OAuth2Providers.Oauth2Provider oauth2Provider = oAuth2Providers.getProvider().get("azure");
        assertThat(oauth2Provider.getAuthorizationUri(), is("http://localhost:8087/oauth2/v2.0/authorize"));
        assertThat(oauth2Provider.getTokenUri(), is("http://localhost:8087/oauth2/v2.0/token"));
        assertThat(oauth2Provider.getUserInfoUri(), is("http://localhost:8087/v1.0/me"));
        assertThat(oauth2Provider.getUserIdAttribute(), is("id"));
        assertThat(oauth2Provider.getUserEmailAttribute(), is("userPrincipalName"));
        assertThat(oauth2Provider.getUserNameAttribute(), is("displayName"));
    }
}