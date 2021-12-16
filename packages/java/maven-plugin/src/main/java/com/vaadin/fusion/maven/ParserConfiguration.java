package com.vaadin.fusion.maven;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.PluginConfiguration;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.utils.ConfigurationList;

public final class ParserConfiguration {
    private ParserClassPathConfiguration classPath;
    private String endpointAnnotation;
    private String openAPIPath;
    private Plugins plugins;

    public Optional<ParserClassPathConfiguration> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public Optional<String> getEndpointAnnotation() {
        return Optional.ofNullable(endpointAnnotation);
    }

    public Optional<String> getOpenAPIPath() {
        return Optional.ofNullable(openAPIPath);
    }

    public Optional<Plugins> getPlugins() {
        return Optional.ofNullable(plugins);
    }

    public static class Plugin {
        private PluginConfiguration configuration;
        private String name;
        private Integer order;

        public Plugin() {
        }

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

    public static class Plugins extends ConfigurationList<Plugin> {
        public Plugins(Collection<Plugin> use, Collection<Plugin> disable) {
            super(use, disable);
        }
    }

    static class PluginsProcessor extends ConfigurationList.Processor<Plugin> {
        private static final Set<Plugin> defaults = Set.of(
                new ParserConfiguration.Plugin(BackbonePlugin.class.getName()));

        public PluginsProcessor(ConfigurationList<Plugin> config) {
            super(config, defaults);
        }
    }
}
