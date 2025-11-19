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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.utils.PluginException;

public abstract class AbstractCompositePlugin<C extends PluginConfiguration>
        extends AbstractPlugin<C> {
    private final LinkedList<Plugin> plugins = new LinkedList<>();

    protected AbstractCompositePlugin(@NonNull Plugin... plugins) {
        Stream.of(plugins).map(Objects::requireNonNull)
                .forEachOrdered(this.plugins::add);
        verifyPluginsOrder();
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        plugins.iterator().forEachRemaining((plugin) -> plugin.enter(nodePath));
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        plugins.descendingIterator()
                .forEachRemaining((plugin) -> plugin.exit(nodePath));
    }

    @Override
    @NonNull
    public Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        for (var plugin : plugins) {
            node = plugin.resolve(node, parentPath);
        }
        return node;
    }

    @Override
    @NonNull
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        for (var plugin : plugins) {
            nodeDependencies = plugin.scan(nodeDependencies);
        }
        return nodeDependencies;
    }

    @Override
    public void setConfiguration(PluginConfiguration configuration) {
        super.setConfiguration(configuration);
        plugins.iterator().forEachRemaining(
                plugin -> plugin.setConfiguration(configuration));
    }

    @Override
    public void setStorage(SharedStorage storage) {
        super.setStorage(storage);
        plugins.iterator()
                .forEachRemaining(plugin -> plugin.setStorage(storage));
    }

    private void verifyPluginsOrder() {
        var previous = new HashSet<Class<? extends Plugin>>();
        for (var plugin : plugins) {
            for (var requiredPluginCls : plugin.getRequiredPlugins()) {
                if (!previous.contains(requiredPluginCls)) {
                    throw new PluginException(
                            String.format("Plugin %s must " + "be run after %s",
                                    plugin.getClass(), requiredPluginCls));
                }
            }
            previous.add(plugin.getClass());
        }
    }
}
