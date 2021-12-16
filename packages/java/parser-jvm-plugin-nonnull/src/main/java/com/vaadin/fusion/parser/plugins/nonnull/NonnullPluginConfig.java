package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.fusion.parser.core.PluginConfiguration;

public class NonnullPluginConfig implements PluginConfiguration {
    private final Set<String> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<String> use = new HashSet<>();

    public NonnullPluginConfig() {
    }

    public NonnullPluginConfig(Collection<String> use,
            Collection<String> disable) {
        this.disable.addAll(disable);
        this.use.addAll(use);
    }

    public Set<String> getDisable() {
        return disable;
    }

    public Set<String> getUse() {
        return use;
    }

    public boolean isDisableAllDefaults() {
        return disableAllDefaults;
    }

    @Override
    public String toString() {
        return "class NonnullPluginConfig {\n"
            + "  disable: " + disable + "\n"
            + "  disableAllDefaults: " + disableAllDefaults + "\n"
            + "  use: " + use + "\n"
            + "}";
    }
}
