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
package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.HierarchyService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteService {

    private final LabelRepository labelRepository;
    private final LayoutMetaBlockRepository layoutRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final SupplyChainRepository supplyChainRepository;
    private final HierarchyService hierarchyService;
    private final AccountService accountService;

    public void deleteLabel(String labelId) {
        hierarchyService.getSubTree(labelId, HierarchyMode.ALL, -1).ifPresent(
                treeNode -> treeNode.accept(child -> {
                    switch (child.getType()) {
                        case LABEL:
                            labelRepository.deleteById(child.getReferenceId());
                            break;
                        case SUPPLY_CHAIN:
                            deleteSupplyChain(child.getReferenceId());
                            break;
                        case SERVICE_ACCOUNT:
                            deleteServiceAccount(child.getReferenceId());
                            break;
                        default:
                            throw new IllegalArgumentException(child.getType() + "not implemented");
                    }
                    return true;
                })
        );
    }

    public void deleteSupplyChain(String supplyChainId) {
        layoutRepository.deleteBySupplyChainId(supplyChainId);
        linkMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
        approvalConfigurationRepository.deleteBySupplyChainId(supplyChainId);
        supplyChainRepository.delete(supplyChainId);
    }

    public void deleteServiceAccount(String serviceAccountId) {
        accountService.deleteServiceAccount(serviceAccountId);
    }
}
