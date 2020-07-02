package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.AccountInfo;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestAccountInfo;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestAccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.rabobank.argos.domain.account.AccountType.SERVICE_ACCOUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class AccountInfoMapperTest {
    private static final String ACCOUNTID = "accountId";
    private static final String NAME = "name";
    private AccountInfoMapper accountInfoMapper;

    @BeforeEach
    void setup() {
        accountInfoMapper = Mappers.getMapper(AccountInfoMapper.class);
    }

    @Test
    void convertToRestAccountInfo() {
        AccountInfo accountInfo = AccountInfo
                .builder()
                .accountId(ACCOUNTID)
                .accountType(SERVICE_ACCOUNT)
                .pathToRoot(List.of("path", "to", "root"))
                .name(NAME)
                .build();
        RestAccountInfo restAccountInfo = accountInfoMapper.convertToRestAccountInfo(accountInfo);
        assertThat(restAccountInfo.getAccountId(), is(ACCOUNTID));
        assertThat(restAccountInfo.getAccountType(), is(RestAccountType.SERVICE_ACCOUNT));
        assertThat(restAccountInfo.getName(), is(NAME));
        assertThat(restAccountInfo.getPath(), is("root/to/path"));
    }
}