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
 * A part of the parser which handles one feature of the walk over the browser
 * callable classes. For every node, the parser asks the plugins what is below
 * it ({@link #scan}), lets them replace each node found there
 * ({@link #resolve}), and tells them when the walk enters and exits it
 * ({@link #enter}, {@link #exit}). Plugins build on each other through the
 * nodes and what they say about them, not by calling each other.
 *
 * @deprecated Internal API, not deprecated for removal. The Java API of
 *             {@code hilla-typescript-generator} is used only by Hilla itself,
 *             and may change or be removed in any release without notice. Do
 *             not use it in application code or libraries. It is marked
 *             deprecated only to make the compiler warn about such use.
 */
@Deprecated
public interface Plugin {

    /**
     * Called on the way into a node, before the nodes below it are walked,
     * which is where a plugin says what it knows about the node before anything
     * below it is known. A plugin which only says which nodes there are to walk
     * has nothing to do here.
     */
    default void enter(NodePath<?> nodePath) {
    }

    /**
     * Called on the way out of a node, once the nodes below it have been
     * walked, which is where a plugin settles what depends on them. A composite
     * plugin exits its plugins in the opposite order to the one it enters them
     * in.
     */
    default void exit(NodePath<?> nodePath) {
    }

    PluginConfiguration getConfiguration();

    void setConfiguration(PluginConfiguration configuration);

    /**
     * The plugins which have to come before this one in the chain, which a
     * composite plugin checks when it is created.
     */
    default Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return Collections.emptyList();
    }

    /**
     * Replaces a node the walk has found before it is walked, which is how a
     * plugin says what a node really is: a type mapped to another one, or a
     * node carrying more than the one found, such as its Kotlin reflection.
     * Called with the path of the node it is found below, before the node gets
     * its own path, and in the order of the plugins, each one getting what the
     * one before it returned.
     *
     * @param node
     *            the node found
     * @param parentPath
     *            the path of the node it is found below; for a related node,
     *            the path of the root
     * @return the node to walk in its place, or the node itself
     */
    @NonNull
    default Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        return node;
    }

    /**
     * Says what is below a node: its child nodes, walked right below it, and
     * its related nodes, walked once below the root after everything already
     * queued. Called when the walk enters a node, in the order of the plugins,
     * each one adding to what the one before it returned. A node equal to one
     * scanned before gets the same result without being scanned again.
     *
     * @param nodeDependencies
     *            the node with what the plugins before this one found below it
     * @return what is below the node, with what this plugin found added
     */
    @NonNull
    default NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    /**
     * Hands the plugin the configuration of the parser running it.
     */
    void setParserConfig(Parser.Config parserConfig);
}
