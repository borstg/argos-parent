package com.rabobank.argos.service.domain.release;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReleaseServiceImpl implements ReleaseService {

    private final VerificationProvider verificationProvider;
    private final LayoutMetaBlockRepository repository;

    @Override
    public VerificationRunResult createRelease(List<Set<Artifact>> releaseArtifacts) {
        return null;
    }
}
