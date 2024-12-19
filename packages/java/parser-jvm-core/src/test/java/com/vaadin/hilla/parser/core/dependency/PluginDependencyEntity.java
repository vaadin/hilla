package com.vaadin.hilla.parser.core.dependency;

import org.jspecify.annotations.NonNull;

public class PluginDependencyEntity {
    private String pluginDependencyField = "foo";

    @NonNull
    public String getPluginDependencyField() {
        return pluginDependencyField;
    }
}
