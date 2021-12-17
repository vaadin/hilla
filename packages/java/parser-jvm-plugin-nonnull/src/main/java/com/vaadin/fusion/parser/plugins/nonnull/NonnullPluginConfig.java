package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.PluginConfiguration;
import com.vaadin.fusion.parser.utils.ConfigList;

public class NonnullPluginConfig
        implements ConfigList<String>, PluginConfiguration {
    private final Set<String> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<String> use = new HashSet<>();

    public NonnullPluginConfig(@Nonnull Collection<String> use,
            @Nonnull Collection<String> disable) {
        this.disable.addAll(disable);
        this.use.addAll(use);
    }

    @Override
    public Collection<String> getDisable() {
        return disable;
    }

    @Override
    public Collection<String> getUse() {
        return use;
    }

    @Override
    public boolean isDisableAllDefaults() {
        return disableAllDefaults;
    }

    static class Processor extends ConfigList.Processor<String> {
        private static final Set<String> defaults = Set.of(
                "javax.annotation.Nonnull", "org.jetbrains.annotations.NotNull",
                "lombok.NonNull", "androidx.annotation.NonNull",
                "org.eclipse.jdt.annotation.NonNull");

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }
}
