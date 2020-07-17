/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.domain.release;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReleaseDossierMetaDataTest {

    protected static final String HASH = "71ed24f24e838b18a4bc53aac2638155692b43289ca9778c37139859fc6e619d";
    protected static final List<String> ARTIFACT_LIST;

    static {
        ARTIFACT_LIST = new ArrayList<>();
        ARTIFACT_LIST.add("string");
        ARTIFACT_LIST.add("string2");
    }

    @Test
    void createHashFromArtifactList() {
        String result = ReleaseDossierMetaData.createHashFromArtifactList(ARTIFACT_LIST);
        assertThat(result, is(HASH));
    }
}