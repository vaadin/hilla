package dev.hilla.internal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GeneratorConfiguration {
    private Plugins plugins;

    public Optional<Plugins> getPlugins() {
        return Optional.ofNullable(plugins);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (GeneratorConfiguration) o;
        return Objects.equals(plugins, that.plugins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugins);
    }

    void setPlugins(Plugins plugins) {
        this.plugins = plugins;
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

    public static class Plugins {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Plugins plugins = (Plugins) o;
            return disableAllDefaults == plugins.disableAllDefaults
                    && Objects.equals(disable, plugins.disable)
                    && Objects.equals(use, plugins.use);
        }

        @Override
        public int hashCode() {
            return Objects.hash(disable, disableAllDefaults, use);
        }
    }
}
