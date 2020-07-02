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

import com.rabobank.argos.domain.account.AccountInfo;
import com.rabobank.argos.domain.account.AccountKeyInfo;
import com.rabobank.argos.domain.account.AccountType;
import com.rabobank.argos.service.domain.account.AccountInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.CONTAINING;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountInfoRepositoryImpl implements AccountInfoRepository {
    protected static final String ACCOUNTS_KEYINFO_VIEW = "accounts-keyinfo";
    protected static final String ACCOUNTS_INFO_VIEW = "accounts-info";
    private static final String CASE_INSENSITIVE = "i";
    private final MongoTemplate template;
    static final String ACCOUNT_KEY_ID_FIELD = "key.keyId";

    @Override
    public List<AccountKeyInfo> findByKeyIds(List<String> keyIds) {
        Criteria rootCriteria = Criteria.where(ACCOUNT_KEY_ID_FIELD).in(keyIds);
        Query query = new Query(rootCriteria);
        return template.find(query, AccountKeyInfo.class, ACCOUNTS_KEYINFO_VIEW);
    }

    @Override
    public List<AccountInfo> findByNameIdPathToRootAndAccountType(String name, List<String> idPathToRoot, AccountType accountType) {
        Criteria criteria = where("name").regex(requireNonNull(MongoRegexCreator.INSTANCE.toRegularExpression(name, CONTAINING)), CASE_INSENSITIVE);
        criteria.orOperator(where("parentlabelId").is(null), where("parentlabelId").in(idPathToRoot));
        if (accountType != null) {
            criteria.and("accountType")
                    .is(accountType.name());
        }
        return template.find(new Query(criteria), AccountInfo.class, ACCOUNTS_INFO_VIEW);
    }
}
