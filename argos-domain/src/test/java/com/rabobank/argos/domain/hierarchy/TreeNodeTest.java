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
package com.rabobank.argos.domain.hierarchy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.rabobank.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_ENTER;
import static com.rabobank.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_EXIT;
import static com.rabobank.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_LEAF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TreeNodeTest {
    private TreeNode treeNode;

    @BeforeEach
    void setUp() {

        TreeNode leafNode = TreeNode.builder()
                .name("servicaccount")
                .type(TreeNode.Type.SERVICE_ACCOUNT)
                .build();

        TreeNode childNode = TreeNode.builder()
                .name("childLabel")
                .children(Collections.singletonList(leafNode))
                .type(TreeNode.Type.LABEL).build();

        treeNode = TreeNode.builder()
                .name("root")
                .children(Collections.singletonList(childNode))
                .type(TreeNode.Type.LABEL)
                .build();

    }

    @Test
    void accept() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        treeNode.accept(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_LEAF), is(1));
    }

    static class TestVisitor implements TreeNodeVisitor<Map<String, Integer>> {
        protected static final String VISIT_ENTER = "visitEnter";
        protected static final String VISIT_EXIT = "visitExit";
        protected static final String VISIT_LEAF = "visitLeaf";
        private Map<String, Integer> visits = new HashMap<>();

        public TestVisitor() {
            visits.put(VISIT_ENTER, 0);
            visits.put(VISIT_EXIT, 0);
            visits.put(VISIT_LEAF, 0);
        }

        @Override
        public boolean visitEnter(TreeNode treeNode) {
            visits.put(VISIT_ENTER, visits.get(VISIT_ENTER) + 1);
            return true;
        }

        @Override
        public boolean visitExit(TreeNode treeNode) {
            visits.put(VISIT_EXIT, visits.get(VISIT_EXIT) + 1);
            return true;
        }

        @Override
        public boolean visitLeaf(TreeNode treeNode) {
            visits.put(VISIT_LEAF, visits.get(VISIT_LEAF) + 1);
            return true;
        }

        @Override
        public Map<String, Integer> result() {
            return visits;
        }
    }
}