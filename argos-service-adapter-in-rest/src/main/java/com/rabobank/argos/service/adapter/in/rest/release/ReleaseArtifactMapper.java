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
package com.rabobank.argos.service.adapter.in.rest.release;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ReleaseArtifactMapper {

    List<Set<Artifact>> mapToArtifacts(List<List<RestArtifact>> restArtifacts);

    Set<Artifact> mapToSetArtifacts(List<RestArtifact> restArtifacts);

}
