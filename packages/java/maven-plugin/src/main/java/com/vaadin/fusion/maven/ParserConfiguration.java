package com.vaadin.fusion.maven;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.vaadin.fusion.parser.core.PluginConfiguration;

public final class ParserConfiguration {
    private ParserClassPathConfiguration classPath;
    private String endpointAnnotation;
    private String openAPIPath;
    private PluginList plugins;

    public Optional<ParserClassPathConfiguration> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public Optional<String> getEndpointAnnotation() {
        return Optional.ofNullable(endpointAnnotation);
    }

    public Optional<String> getOpenAPIPath() {
        return Optional.ofNullable(openAPIPath);
    }

    public Optional<PluginList> getPlugins() {
        return Optional.ofNullable(plugins);
    }

    public static class Plugin {
        private String name;
        private PluginConfiguration configuration;
        private Integer order;

        public Plugin() {}

        public Plugin(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Plugin)) {
                return false;
            }

            return Objects.equals(name, ((Plugin) other).name);
        }

        public PluginConfiguration getConfiguration() {
            return configuration;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        public Integer getOrder() {
            return order;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class PluginList {
        private final Set<Plugin> disable = Set.of();
        private final boolean disableAllDefaults = false;
        private final Set<Plugin> use = Set.of();

        public Set<Plugin> getDisable() {
            return disable;
        }

        public Set<Plugin> getUse() {
            return use;
        }

        public boolean isDisableAllDefaults() {
            return disableAllDefaults;
        }
    }
}
