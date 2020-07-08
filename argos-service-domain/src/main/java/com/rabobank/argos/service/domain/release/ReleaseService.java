package com.rabobank.argos.service.domain.release;


import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;

import java.util.List;
import java.util.Set;

public interface ReleaseService {
    VerificationRunResult createRelease(List<Set<Artifact>> releaseArtifacts);
}
