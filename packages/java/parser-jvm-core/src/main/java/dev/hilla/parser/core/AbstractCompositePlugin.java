package dev.hilla.parser.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.PluginException;

public abstract class AbstractCompositePlugin<C extends PluginConfiguration>
        extends AbstractPlugin<C> {
    private final LinkedList<Plugin> plugins = new LinkedList<>();

    protected AbstractCompositePlugin(@Nonnull Plugin... plugins) {
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
            for (var runAfterPluginCls : plugin.runAfter()) {
                if (!previous.contains(runAfterPluginCls)) {
                    throw new PluginException(
                            String.format("Plugin %s must " + "be run after %s",
                                    plugin.getClass(), runAfterPluginCls));
                }
            }
            for (var runBeforePluginCls: plugin.runBefore()) {
                if (previous.contains(runBeforePluginCls)) {
                    throw new PluginException(
                            String.format("Plugin %s must " + "be run before %s",
                                plugin.getClass(), runBeforePluginCls));
                }
            }
            previous.add(plugin.getClass());
        }
    }
}
