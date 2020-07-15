package com.rabobank.argos.service.adapter.out.mongodb.release;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class ReleaseDossierConversionHelperTest {

    protected static final String HASH = "673d0de055dfbb9ed400f13cb1ca5fac";
    protected static final List<String> ARTIFACT_LIST = List.of("string", "string2");

    @Test
    void convertToDocumentList() {
        List<Document> documents = ReleaseDossierConversionHelper.convertToDocumentList(List.of(Set.of("string", "string2")));
        assertThat(documents, hasSize(1));
        Document document = documents.iterator().next();
        assertThat(document.containsKey(HASH), is(true));
        assertThat(document.get(HASH), is(ARTIFACT_LIST));
    }

    @Test
    void createHashFromArtifactList() {
        String result = ReleaseDossierConversionHelper.createHashFromArtifactList(ARTIFACT_LIST);
        assertThat(result, is(HASH));
    }
}