package com.vaadin.fusion.parser.core;

import java.util.Optional;
import java.util.SortedSet;

public class PluginsToolset {
    private final SortedSet<Plugin> plugins;

    public PluginsToolset(SortedSet<Plugin> plugins) {
        this.plugins = plugins;
    }

    public Optional<Plugin> getPluginByClass(Class<? extends Plugin> cls) {
        return plugins.stream()
                .filter(plugin -> cls.isAssignableFrom(plugin.getClass()))
                .findFirst();
    }

    public boolean hasPluginAfter(Plugin current, Plugin maybeAfter) {
        return hasPluginInDirection(current, maybeAfter, Direction.AFTER);
    }

    public boolean hasPluginAfter(Plugin current,
            Class<? extends Plugin> maybeAfter) {
        return hasPluginInDirection(current, maybeAfter, Direction.AFTER);
    }

    public boolean hasPluginAfter(Class<? extends Plugin> current,
            Plugin maybeAfter) {
        return hasPluginInDirection(current, maybeAfter, Direction.AFTER);
    }

    public boolean hasPluginAfter(Class<? extends Plugin> current,
            Class<? extends Plugin> maybeAfter) {
        return hasPluginInDirection(current, maybeAfter, Direction.AFTER);
    }

    public boolean hasPluginBefore(Plugin current, Plugin maybeBefore) {
        return hasPluginInDirection(current, maybeBefore, Direction.BEFORE);
    }

    public boolean hasPluginBefore(Plugin current,
            Class<? extends Plugin> maybeBefore) {
        return hasPluginInDirection(current, maybeBefore, Direction.BEFORE);
    }

    public boolean hasPluginBefore(Class<Plugin> current, Plugin maybeBefore) {
        return hasPluginInDirection(current, maybeBefore, Direction.BEFORE);
    }

    public boolean hasPluginBefore(Class<? extends Plugin> current,
            Class<? extends Plugin> maybeBefore) {
        return hasPluginInDirection(current, maybeBefore, Direction.BEFORE);
    }

    private boolean hasPluginInDirection(Plugin current,
            Plugin maybeInDirection, Direction direction) {
        if (!plugins.contains(current) || !plugins.contains(maybeInDirection)) {
            return false;
        }

        var result = plugins.comparator().compare(maybeInDirection, current);

        return direction == Direction.AFTER ? result > 0 : result < 0;
    }

    private boolean hasPluginInDirection(Plugin current,
            Class<? extends Plugin> maybeInDirection, Direction direction) {
        return getPluginByClass(maybeInDirection)
                .map(plugin -> hasPluginInDirection(current, plugin, direction))
                .orElse(false);
    }

    private boolean hasPluginInDirection(Class<? extends Plugin> current,
            Plugin maybeInDirection, Direction direction) {
        return getPluginByClass(current)
                .map(plugin -> hasPluginInDirection(plugin, maybeInDirection,
                        direction))
                .orElse(false);
    }

    private boolean hasPluginInDirection(Class<? extends Plugin> current,
            Class<? extends Plugin> maybeInDirection, Direction direction) {
        var targetInstance = getPluginByClass(current);
        var maybeBeforeInstance = getPluginByClass(maybeInDirection);

        if (targetInstance.isPresent() && maybeBeforeInstance.isPresent()) {
            return hasPluginInDirection(targetInstance.get(),
                    maybeBeforeInstance.get(), direction);
        }

        return false;
    }

    private enum Direction {
        BEFORE, AFTER,
    }
}
