package com.vaadin.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.utils.ConfigList;

public class NonnullPluginConfig
        implements ConfigList<AnnotationMatcher>, PluginConfiguration {
    private final Set<AnnotationMatcher> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<AnnotationMatcher> use = new HashSet<>();

    public NonnullPluginConfig() {
    }

    public NonnullPluginConfig(Collection<AnnotationMatcher> use,
            Collection<AnnotationMatcher> disable) {
        if (disable != null) {
            this.disable.addAll(disable);
        }

        if (use != null) {
            this.use.addAll(use);
        }
    }

    @Override
    public Collection<AnnotationMatcher> getDisabledOptions() {
        return disable;
    }

    @Override
    public Collection<AnnotationMatcher> getUsedOptions() {
        return use;
    }

    @Override
    public boolean shouldAllDefaultsBeDisabled() {
        return disableAllDefaults;
    }

    static class Processor extends ConfigList.Processor<AnnotationMatcher> {
        static final Set<AnnotationMatcher> defaults = Set.of(
                // Package-level annotations have low score
                new AnnotationMatcher("org.springframework.lang.NonNullApi",
                        false, 10),
                // Id and Version annotation usually mark nullable fields for
                // CRUD operations.
                // Low score allows other annotations to override them.
                new AnnotationMatcher("jakarta.persistence.Id", true, 20),
                new AnnotationMatcher("jakarta.persistence.Version", true, 20),
                // Nullable-like annotations get a higher score. This should
                // only matter when they are used in conjunction with
                // package-level annotations
                new AnnotationMatcher("jakarta.annotation.Nullable", true, 20),
                new AnnotationMatcher("com.vaadin.hilla.Nullable", true, 20),
                new AnnotationMatcher("org.springframework.lang.Nullable", true,
                        20),
                new AnnotationMatcher("org.jspecify.annotations.Nullable", true,
                        20),
                // Nonnull-like annotations have the highest score for
                // compatibility with the old generator
                new AnnotationMatcher("jakarta.annotation.Nonnull", false, 30),
                new AnnotationMatcher("javax.annotation.Nonnull", false, 30),
                new AnnotationMatcher("com.vaadin.hilla.Nonnull", false, 30),
                new AnnotationMatcher("org.springframework.lang.NonNull", false,
                        30),
                new AnnotationMatcher("org.jspecify.annotations.NonNull", false,
                        30));

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }

}
