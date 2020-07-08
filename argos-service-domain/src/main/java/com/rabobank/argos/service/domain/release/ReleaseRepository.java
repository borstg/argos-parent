package com.rabobank.argos.service.domain.release;

import com.rabobank.argos.domain.release.ReleaseFile;
import com.rabobank.argos.domain.release.ReleaseFileMetaData;

import java.util.List;
import java.util.Optional;

public interface ReleaseRepository {
    void storeRelease(ReleaseFileMetaData releaseFileMetaData, ReleaseFile releaseFile);

    Optional<ReleaseFileMetaData> findReleaseByReleasedArtifactsAndPath(List<String> releasedArtifacts, String path);

    Optional<ReleaseFile> getReleaseFileById(String id);
}
