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
 * the files, and another one, which comes after it in the chain and so exits a
 * node before it, contributes to them.
 */
final class RenderPlugins extends AbstractCompositePlugin<PluginConfiguration> {
    RenderPlugins() {
        this(new ExitRecorder());
    }

    private RenderPlugins(ExitRecorder recorder) {
        super(recorder, new BasicPlugin(), new EmitterPlugin(),
                new ContributorPlugin(recorder));
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

    /**
     * Records the members it exits, which is the last thing to happen to a node
     * before it is rendered, as it comes first in the chain.
     */
    private static final class ExitRecorder
            extends AbstractPlugin<PluginConfiguration> {
        private final List<Object> exited = new ArrayList<>();

        @Override
        public void exit(NodePath<?> nodePath) {
            exited.add(nodePath.getNode().getSource());
        }
    }

    private static final class ContributorPlugin
            extends AbstractPlugin<PluginConfiguration> {
        private final ExitRecorder recorder;

        ContributorPlugin(ExitRecorder recorder) {
            this.recorder = recorder;
        }

        @Override
        public void contribute(NodePath<?> nodePath, Output output) {
            var source = nodePath.getNode().getSource();
            output.of(nodePath.getNode(), Steps.class)
                    .forEach(steps -> steps.add(
                            "contributed" + (recorder.exited.contains(source)
                                    ? " after every exit"
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
