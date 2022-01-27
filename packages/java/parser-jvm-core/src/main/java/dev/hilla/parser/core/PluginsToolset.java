package dev.hilla.parser.core;

import java.util.Optional;
import java.util.SortedSet;

public class PluginsToolset {
    private final SortedSet<Plugin> plugins;

    public PluginsToolset(SortedSet<Plugin> plugins) {
        this.plugins = plugins;
    }

    public Optional<Integer> comparePluginOrders(Plugin current,
            Plugin target) {
        if (!plugins.contains(current) || !plugins.contains(target)) {
            return Optional.empty();
        }

        return Optional.of(plugins.comparator().compare(current, target));
    }

    public Optional<Integer> comparePluginOrders(Plugin current,
            Class<? extends Plugin> target) {
        return findPluginByClass(target)
                .flatMap(plugin -> comparePluginOrders(current, plugin));
    }

    public Optional<Integer> comparePluginOrders(
            Class<? extends Plugin> current, Plugin target) {
        return findPluginByClass(current)
                .flatMap(plugin -> comparePluginOrders(plugin, target));
    }

    public Optional<Integer> comparePluginOrders(
            Class<? extends Plugin> current, Class<? extends Plugin> target) {
        var currentInstance = findPluginByClass(current);
        var targetInstance = findPluginByClass(target);

        if (currentInstance.isPresent() && targetInstance.isPresent()) {
            return comparePluginOrders(currentInstance.get(),
                    targetInstance.get());
        }

        return Optional.empty();
    }

    public Optional<Plugin> findPluginByClass(Class<? extends Plugin> cls) {
        return plugins.stream()
                .filter(plugin -> cls.isAssignableFrom(plugin.getClass()))
                .findFirst();
    }
}
