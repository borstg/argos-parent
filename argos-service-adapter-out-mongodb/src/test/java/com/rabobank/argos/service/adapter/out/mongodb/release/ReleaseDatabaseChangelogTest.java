package com.rabobank.argos.service.adapter.out.mongodb.release;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.COLLECTION_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseDatabaseChangelogTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private IndexOperations indexOperations;

    private ReleaseDatabaseChangelog releaseDatabaseChangelog;

    @BeforeEach
    void setUp() {
        releaseDatabaseChangelog = new ReleaseDatabaseChangelog();
    }

    @Test
    void addIndex() {
        when(mongoTemplate.indexOps(COLLECTION_NAME)).thenReturn(indexOperations);
        releaseDatabaseChangelog.addIndex(mongoTemplate);
        verify(indexOperations, times(2)).ensureIndex(any());
    }
}