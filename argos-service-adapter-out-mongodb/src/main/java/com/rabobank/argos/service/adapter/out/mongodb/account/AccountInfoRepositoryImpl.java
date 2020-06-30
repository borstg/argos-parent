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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.rabobank.argos.domain.account.AccountKeyInfo;
import com.rabobank.argos.service.domain.account.AccountInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountInfoRepositoryImpl implements AccountInfoRepository {
    private final MongoTemplate template;
    static final String ACCOUNT_KEY_ID_FIELD = "key.keyId";

    @Override
    public List<AccountKeyInfo> findByKeyIds(List<String> keyIds) {
        Criteria rootCriteria = Criteria.where(ACCOUNT_KEY_ID_FIELD).in(keyIds);
        Query query = new Query(rootCriteria);
        log.info(query.toString());
        return template.find(query, AccountKeyInfo.class, "accounts-keyinfo");
    }
}
