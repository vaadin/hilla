package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.utils.ConfigList;

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
                // Nullable-like annotations get a higher score. This should
                // only matter when they are used in conjunction with
                // package-level annotations
                new AnnotationMatcher("javax.annotation.Nullable", true, 20),
                new AnnotationMatcher("org.jetbrains.annotations.Nullable",
                        true, 20),
                new AnnotationMatcher("androidx.annotation.Nullable", true, 20),
                new AnnotationMatcher("org.eclipse.jdt.annotation.Nullable",
                        true, 20),
                // Nonnull-like annotations have the highest score for
                // compatibility with the old generator
                new AnnotationMatcher("javax.annotation.Nonnull", false, 30),
                new AnnotationMatcher("org.jetbrains.annotations.NotNull",
                        false, 30),
                new AnnotationMatcher("lombok.NonNull", false, 30),
                new AnnotationMatcher("androidx.annotation.NonNull", false, 30),
                new AnnotationMatcher("org.eclipse.jdt.annotation.NonNull",
                        false, 30),
                new AnnotationMatcher("dev.hilla.Nonnull", false, 30));

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }

}
