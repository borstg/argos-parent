package com.rabobank.argos.service.adapter.out.mongodb.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rabobank.argos.domain.release.ReleaseFile;
import com.rabobank.argos.domain.release.ReleaseFileMetaData;
import com.rabobank.argos.service.domain.release.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReleaseRepositoryImpl implements ReleaseRepository {
    private final GridFsTemplate gridFsTemplate;

    //private final MongoTemplate mongoTemplate;
    @SneakyThrows
    @Override
    public void storeRelease(ReleaseFileMetaData releaseFileMetaData, ReleaseFile releaseFile) {
        DBObject metaData = new BasicDBObject();
        metaData.put("releaseArtifacts", releaseFileMetaData.getReleaseArtifacts());
        metaData.put("supplyChainPath", releaseFileMetaData.getSupplyChainPath());

        String jsonReleaseFile = new ObjectMapper().writeValueAsString(releaseFile);
        InputStream inputStream = IOUtils.toInputStream(jsonReleaseFile, StandardCharsets.UTF_8);
        String fileName = "release-" + releaseFileMetaData.getSupplyChainPath() + "-" + new Date().toInstant().getEpochSecond() + ".json";
        gridFsTemplate.store(inputStream, fileName, "application/json", metaData);
    }

    @Override
    public Optional<ReleaseFileMetaData> findReleaseByReleasedArtifactsAndPath(List<String> releasedArtifacts, String path) {
        throw new NotImplementedException("findReleaseByReleasedArtifactsAndPath not implemented");
    }

    @Override
    public Optional<ReleaseFile> getReleaseFileById(String id) {
        throw new NotImplementedException("getReleaseFileById not implemented");
    }
}
