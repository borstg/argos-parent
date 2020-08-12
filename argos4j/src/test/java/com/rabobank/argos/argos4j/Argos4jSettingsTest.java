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
package com.rabobank.argos.argos4j;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabobank.argos.domain.ArgosError;

class Argos4jSettingsTest {
    
    private Path ARGOS_SETTINGS_FILE_PATH = Paths.get("src","test","resources","argos-settings.json");
    private Map<String, Map<String,String>> configMaps;
    private List<ReleaseCollector> releaseCollectors1;
    private List<ReleaseCollector> releaseCollectors2;

    @BeforeEach
    void setUp() throws Exception {
        configMaps = new HashMap<>();
        Map<String,String> xldCollector = new HashMap<String,String>();
        xldCollector.put("username", "username");
        xldCollector.put("password", "password");
        xldCollector.put("applicationVersion", "version");
        Map<String,String> gitCollector = new HashMap<String,String>();
        gitCollector.put("foo", "foo");
        gitCollector.put("bar", "bar");
        configMaps.put("xld-collector", xldCollector);
        configMaps.put("git-collector", gitCollector);
        releaseCollectors1 = new ArrayList<>();
        releaseCollectors1.add(
                ReleaseCollector.builder()
                .name("local-file-collector")
                .collector(LocalFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        releaseCollectors1.add(
                ReleaseCollector.builder()
                .name("local-zip-file-collector")
                .collector(LocalZipFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("xld-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://xld-collector"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("git-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://git-collector"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("remote-file-collector")
                .collector(RemoteFileCollector.builder()
                        .url(new URL("http://remote-file-collector"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("remote-zip-file-collector")
                .collector(RemoteZipFileCollector.builder()
                        .url(new URL("http://remote-zip-file-collector"))
                        .build()).build());
        releaseCollectors2 = new ArrayList<>();
        releaseCollectors2.add(
                ReleaseCollector.builder()
                .name("local-collector")
                .collector(LocalFileCollector.builder()
                        .path(Paths.get("bar"))
                        .build()).build());
        releaseCollectors2.add(ReleaseCollector.builder()
                .name("xld-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://xld-collector"))
                        .parameterMap(xldCollector)
                        .build()).build());
        releaseCollectors2.add(ReleaseCollector.builder()
                .name("git-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://git-collector"))
                        .parameterMap(gitCollector)
                        .build()).build());
        
    }
    
    @Test
    void readSettingsTest() {
        Argos4jSettings expectedSettings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors1)
                .build();
        Argos4jSettings settings = Argos4jSettings.readSettings(ARGOS_SETTINGS_FILE_PATH);
        assertEquals(expectedSettings, settings);
    }

    @Test
    void enrichReleaseCollectorsTest() {
        Argos4jSettings expectedSettings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors2)
                .build();
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors2)
                .build();
        settings.enrichReleaseCollectors(configMaps);
        assertEquals(expectedSettings, settings);
    }
    
    @Test
    void throwErrorReadingFileTest() {
        Argos4jError argosError = assertThrows(Argos4jError.class, () -> {
            Argos4jSettings.readSettings(Paths.get("zomaar-wat"));
        });
        assertThat(argosError.getMessage(), startsWith("Error on reading config file zomaar-wat: "));
    }

}
