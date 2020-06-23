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
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.HierarchyService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LayoutMetaBlockRepository layoutRepository;

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private ApprovalConfigurationRepository approvalConfigurationRepository;

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private HierarchyService hierarchyService;

    @Mock
    private AccountService accountService;

    @Mock
    private TreeNode treeNode;

    @Mock
    private TreeNode labelTreeNode;

    @Mock
    private TreeNode supplyChainTreeNode;

    @Mock
    private TreeNode serviceAccountTreeNode;

    private final static String ID = "id";

    private DeleteService service;

    @BeforeEach
    void setUp() {
        service = new DeleteService(labelRepository, layoutRepository, linkMetaBlockRepository, approvalConfigurationRepository, supplyChainRepository, hierarchyService, accountService);
    }

    @Test
    void deleteLabel() {
        when(labelTreeNode.getType()).thenReturn(TreeNode.Type.LABEL);
        when(labelTreeNode.getReferenceId()).thenReturn("labelId");
        when(supplyChainTreeNode.getType()).thenReturn(TreeNode.Type.SUPPLY_CHAIN);
        when(supplyChainTreeNode.getReferenceId()).thenReturn("supplyChainId");
        when(serviceAccountTreeNode.getType()).thenReturn(TreeNode.Type.SERVICE_ACCOUNT);
        when(serviceAccountTreeNode.getReferenceId()).thenReturn("serviceAccountId");
        doAnswer(invocation -> {
            TreeNodeVisitor treeNodeVisitor = invocation.getArgument(0);
            treeNodeVisitor.visitExit(labelTreeNode);
            treeNodeVisitor.visitExit(supplyChainTreeNode);
            treeNodeVisitor.visitExit(serviceAccountTreeNode);
            return null;
        }).when(treeNode).accept(any());

        when(hierarchyService.getSubTree(ID, HierarchyMode.ALL, -1)).thenReturn(Optional.of(treeNode));
        service.deleteLabel(ID);
        verify(labelRepository).deleteById("labelId");
        verify(supplyChainRepository).delete("supplyChainId");
        verify(layoutRepository).deleteBySupplyChainId("supplyChainId");
        verify(linkMetaBlockRepository).deleteBySupplyChainId("supplyChainId");
        verify(approvalConfigurationRepository).deleteBySupplyChainId("supplyChainId");
        verify(accountService).deleteServiceAccount("serviceAccountId");
    }

    @Test
    void deleteSupplyChain() {
        service.deleteSupplyChain(ID);
        verify(supplyChainRepository).delete(ID);
        verify(layoutRepository).deleteBySupplyChainId(ID);
        verify(linkMetaBlockRepository).deleteBySupplyChainId(ID);
        verify(approvalConfigurationRepository).deleteBySupplyChainId(ID);
    }

    @Test
    void deleteServiceAccount() {
        service.deleteServiceAccount(ID);
        verify(accountService).deleteServiceAccount(ID);
    }
}