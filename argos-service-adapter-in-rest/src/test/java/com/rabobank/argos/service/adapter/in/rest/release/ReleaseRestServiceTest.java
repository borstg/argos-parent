package com.rabobank.argos.service.adapter.in.rest.release;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.release.ReleaseResult;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestReleaseArtifacts;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestReleaseResult;
import com.rabobank.argos.service.domain.release.ReleaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReleaseRestServiceTest {
    @Mock
    private ReleaseService releaseService;
    @Mock
    private ReleaseArtifactMapper artifactMapper;
    @Mock
    private ReleaseResultMapper releaseResultMapper;

    @Mock
    private RestReleaseArtifacts restReleaseArtifacts;

    @Mock
    private RestArtifact restArtifact;

    @Mock
    private ReleaseResult releaseResult;

    @Mock
    private RestReleaseResult restReleaseResult;

    @Mock
    private Artifact artifact;


    ReleaseRestService releaseRestService;


    @BeforeEach
    void setUp() {
        releaseRestService = new ReleaseRestService(releaseService, artifactMapper, releaseResultMapper);
    }

    @Test
    void createReleaseShouldReturn200() {
        when(releaseService.createRelease(any(), any())).thenReturn(releaseResult);
        when(artifactMapper.mapToArtifacts(singletonList(singletonList(restArtifact))))
                .thenReturn(singletonList(Set.of(artifact)));
        when(releaseResultMapper.maptoRestReleaseResult(releaseResult)).thenReturn(restReleaseResult);
        when(restReleaseArtifacts.getReleaseArtifacts()).thenReturn(singletonList(singletonList(restArtifact)));
        ResponseEntity<RestReleaseResult> result = releaseRestService.createRelease("id", restReleaseArtifacts);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}