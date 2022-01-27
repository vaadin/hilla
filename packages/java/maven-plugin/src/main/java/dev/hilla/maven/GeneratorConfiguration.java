package dev.hilla.maven;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GeneratorConfiguration {
    private String outputDir;
    private PluginList plugins;

    public Optional<String> getOutputDir() {
        return Optional.ofNullable(outputDir);
    }

    public Optional<PluginList> getPlugins() {
        return Optional.ofNullable(plugins);
    }

    public static class Plugin {
        private final String path;

        public Plugin(String path) {
            this.path = path;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Plugin)) {
                return false;
            }

            return Objects.equals(path, ((Plugin) other).path);
        }

        public String getPath() {
            return path;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
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
