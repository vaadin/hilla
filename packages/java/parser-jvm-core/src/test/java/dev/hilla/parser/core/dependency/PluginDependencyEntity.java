package dev.hilla.parser.core.dependency;

import jakarta.annotation.Nonnull;

public class PluginDependencyEntity {
    private String pluginDependencyField = "foo";

    @Nonnull
    public String getPluginDependencyField() {
        return pluginDependencyField;
    }
}
