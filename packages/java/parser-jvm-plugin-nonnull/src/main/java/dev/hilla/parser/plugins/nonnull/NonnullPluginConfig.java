package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.utils.ConfigList;

public class NonnullPluginConfig
        implements ConfigList<String>, PluginConfiguration {
    private final Set<String> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<String> use = new HashSet<>();

    public NonnullPluginConfig() {
    }

    public NonnullPluginConfig(Collection<String> use,
            Collection<String> disable) {
        if (disable != null) {
            this.disable.addAll(disable);
        }

        if (use != null) {
            this.use.addAll(use);
        }
    }

    @Override
    public Collection<String> getDisabledOptions() {
        return disable;
    }

    @Override
    public Collection<String> getUsedOptions() {
        return use;
    }

    @Override
    public boolean shouldAllDefaultsBeDisabled() {
        return disableAllDefaults;
    }

    static class Processor extends ConfigList.Processor<String> {
        static final Set<String> defaults = Set.of("javax.annotation.Nonnull",
                "org.jetbrains.annotations.NotNull", "lombok.NonNull",
                "androidx.annotation.NonNull",
                "org.eclipse.jdt.annotation.NonNull", "dev.hilla.Nonnull");

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }
}
