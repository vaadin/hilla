package com.vaadin.fusion.maven;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GeneratorConfiguration {
    private PluginList plugins;
    private String outputDir;

    public Optional<String> getOutputDir() {
        return Optional.ofNullable(outputDir);
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
