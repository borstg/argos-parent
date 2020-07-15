package com.rabobank.argos.service.adapter.out.mongodb.release;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class ReleaseDossierConversionHelper {
    private ReleaseDossierConversionHelper() {
    }

    public static List<Document> convertToDocumentList(List<Set<String>> releaseArtifacts) {
        return releaseArtifacts.stream().map(artifactSet -> {
            List<String> artifactList = new ArrayList<>(artifactSet);
            Collections.sort(artifactList);
            Document document = new Document();
            document.put(createHashFromArtifactList(artifactList), artifactList);
            return document;
        }).collect(Collectors.toList());

    }

    public static String createHashFromArtifactList(List<String> artifactList) {
        return md5Hex(join("", artifactList));
    }
}
