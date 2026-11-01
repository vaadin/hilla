/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.core.basic;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.hilla.parser.core.AbstractCompositePlugin;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Output;
import com.vaadin.hilla.parser.core.PluginConfiguration;

/**
 * A plugin chain rendering a file for every member of a class: one plugin owns
 * the files, and another one, which comes before it in the chain, contributes
 * to them.
 */
final class RenderPlugins extends AbstractCompositePlugin<PluginConfiguration> {
    RenderPlugins() {
        super(new BasicPlugin(), new ContributorPlugin(), new EmitterPlugin());
    }

    /**
     * Lists the steps a file takes until it is printed, which is what the
     * contributor adds to.
     */
    static final class Steps implements Output.Document {
        private final List<String> steps = new ArrayList<>();

        Steps(String first) {
            steps.add(first);
        }

        void add(String step) {
            steps.add(step);
        }

        @Override
        public String print() {
            return String.join(", ", steps);
        }
    }

    private static final class ContributorPlugin
            extends AbstractPlugin<PluginConfiguration> {
        private final List<Member> exited = new ArrayList<>();

        @Override
        public void exit(NodePath<?> nodePath) {
            if (nodePath.getNode().getSource() instanceof Member member) {
                exited.add(member);
            }
        }

        @Override
        public void contribute(NodePath<?> nodePath, Output output) {
            output.of(nodePath.getNode(), Steps.class)
                    .forEach(steps -> steps.add("contributed"
                            + (exited.contains(nodePath.getNode().getSource())
                                    ? " after exit"
                                    : "")));
        }
    }

    /**
     * Emits a file for every member, named after it, so that two members of the
     * same name are written to the same file.
     */
    private static final class EmitterPlugin
            extends AbstractPlugin<PluginConfiguration> {
        @Override
        public void emit(NodePath<?> nodePath, Output output) {
            if (nodePath.getNode().getSource() instanceof Member member) {
                output.emit(nodePath.getNode(),
                        member.getName().replaceAll("^(field|method)", "")
                                .toLowerCase() + ".txt",
                        new Steps("emitted for " + member.getName()));
            }
        }
    }
}
