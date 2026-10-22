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
package com.vaadin.hilla.parser.core;

import java.util.Collection;
import java.util.Collections;

import org.jspecify.annotations.NonNull;

/**
 * <b>Internal API.</b> Not part of the public Hilla API: used only by Hilla
 * itself, and may change or be removed in any release without notice or
 * deprecation. Do not use it in application code or libraries.
 */
public interface Plugin {

    /**
     * Called on the way into a node, before the nodes below it are walked. A
     * plugin which only says which nodes there are to walk has nothing to do
     * here.
     */
    default void enter(NodePath<?> nodePath) {
    }

    /**
     * Called on the way out of a node, once the nodes below it have been
     * walked.
     */
    default void exit(NodePath<?> nodePath) {
    }

    PluginConfiguration getConfiguration();

    void setConfiguration(PluginConfiguration configuration);

    default Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return Collections.emptyList();
    }

    @NonNull
    default Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        return node;
    }

    @NonNull
    default NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    /**
     * Hands the plugin the configuration of the parser running it.
     */
    void setParserConfig(Parser.Config parserConfig);
}
