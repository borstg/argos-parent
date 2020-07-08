package com.rabobank.argos.service.adapter.in.rest.release;

import com.rabobank.argos.service.adapter.in.rest.api.handler.ReleaseApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestReleaseArtifacts;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.domain.release.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReleaseRestService implements ReleaseApi {

    private final ReleaseService releaseService;

    @Override
    public ResponseEntity<RestVerificationResult> createRelease(String supplyChainId, @Valid RestReleaseArtifacts restReleaseArtifacts) {
        return null;
    }
}
