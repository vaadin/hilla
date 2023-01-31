package dev.hilla.parser.core;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import dev.hilla.parser.utils.PluginException;

import jakarta.annotation.Nonnull;

public abstract class AbstractCompositePlugin<C extends PluginConfiguration>
        extends AbstractPlugin<C> {
    private final LinkedList<Plugin> plugins = new LinkedList<>();

    protected AbstractCompositePlugin(@Nonnull Plugin... plugins) {
        Stream.of(plugins).map(Objects::requireNonNull)
                .sorted(Comparator.comparingInt(Plugin::getOrder))
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
    @Nonnull
    public Node<?, ?> resolve(@Nonnull Node<?, ?> node,
            @Nonnull NodePath<?> parentPath) {
        for (var plugin : plugins) {
            node = plugin.resolve(node, parentPath);
        }
        return node;
    }

    @Override
    @Nonnull
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        for (var plugin : plugins) {
            nodeDependencies = plugin.scan(nodeDependencies);
        }
        return nodeDependencies;
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
