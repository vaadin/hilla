package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Set;

import com.vaadin.fusion.parser.core.PluginConfiguration;
import com.vaadin.fusion.parser.utils.ConfigurationList;

public class NonnullPluginConfig extends ConfigurationList<String>
        implements PluginConfiguration {
    public NonnullPluginConfig(Collection<String> use,
            Collection<String> disable) {
        super(use, disable);
    }

    static class Processor extends ConfigurationList.Processor<String> {
        private static final Set<String> defaults = Set.of(
                "javax.annotation.Nonnull", "org.jetbrains.annotations.NotNull",
                "lombok.NonNull", "androidx.annotation.NonNull",
                "org.eclipse.jdt.annotation.NonNull");

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }
}
