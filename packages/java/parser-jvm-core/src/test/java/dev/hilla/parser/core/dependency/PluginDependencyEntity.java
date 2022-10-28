package dev.hilla.parser.core.dependency;

import dev.hilla.shared.Nonnull;

public class PluginDependencyEntity {
    private String pluginDependencyField = "foo";

    @Nonnull
    public String getPluginDependencyField() {
        return pluginDependencyField;
    }
}
