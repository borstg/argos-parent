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
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.AccountKeyInfo;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo;
import com.rabobank.argos.service.domain.account.AccountInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAccountRestserviceTest {
    protected static final String KEY_ID = "keyId";
    @Mock
    private AccountInfoRepository accountInfoRepository;
    @Mock
    private AccountKeyInfoMapper accountKeyInfoMapper;

    @Mock
    private AccountKeyInfo accountKeyInfo;

    @Mock
    private RestAccountKeyInfo restAccountKeyInfo;

    private SearchAccountRestservice searchAccountRestservice;

    @BeforeEach
    void setUp() {
        searchAccountRestservice = new SearchAccountRestservice(accountInfoRepository, accountKeyInfoMapper);
    }

    @Test
    void searchKeysFromAccountShouldReturn200() {
        when(accountInfoRepository.findByKeyIds(any())).thenReturn(Collections.singletonList(accountKeyInfo));
        when(restAccountKeyInfo.getKeyId()).thenReturn(KEY_ID);
        when(accountKeyInfoMapper.convertToRestAccountInfo(accountKeyInfo)).thenReturn(restAccountKeyInfo);
        ResponseEntity<List<RestAccountKeyInfo>> responseEntity = searchAccountRestservice.searchKeysFromAccount("supplyChainId", Collections.singletonList(KEY_ID));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }
}