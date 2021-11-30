package com.vaadin.fusion.maven;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        private final String name;

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

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class PluginList {
        private final List<Plugin> disable = List.of();
        private final boolean disableAllDefaults = false;
        private final List<Plugin> use = List.of();

        public List<Plugin> getDisable() {
            return disable;
        }

        public List<Plugin> getUse() {
            return use;
        }

        public boolean isDisableAllDefaults() {
            return disableAllDefaults;
        }
    }
}
