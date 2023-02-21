package dev.hilla.engine;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dev.hilla.parser.utils.ConfigList;

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
        private String path;

        // Maven and Jackson requires a constructor for deserialization
        public Plugin() {
        }

        Plugin(String path) {
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

    public static class Plugins implements ConfigList<Plugin> {
        private final List<Plugin> disable = new ArrayList<>();
        private final boolean disableAllDefaults;
        private final List<Plugin> use = new ArrayList<>();

        // Maven (and Jackson) require a constructor for deserialization
        public Plugins() {
            disableAllDefaults = false;
        }

        Plugins(@Nonnull Collection<Plugin> use,
                @Nonnull Collection<Plugin> disable,
                boolean disableAllDefaults) {
            this.use.addAll(use);
            this.disable.addAll(disable);
            this.disableAllDefaults = disableAllDefaults;
        }

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
            var plugins = (Plugins) o;
            return disableAllDefaults == plugins.disableAllDefaults
                    && Objects.equals(disable, plugins.disable)
                    && Objects.equals(use, plugins.use);
        }

        @Override
        public int hashCode() {
            return Objects.hash(disable, disableAllDefaults, use);
        }

        @Override
        public Collection<Plugin> getDisabledOptions() {
            return disable;
        }

        @Override
        public Collection<Plugin> getUsedOptions() {
            return use;
        }

        @Override
        public boolean shouldAllDefaultsBeDisabled() {
            return disableAllDefaults;
        }
    }

    static class PluginsProcessor extends ConfigList.Processor<Plugin> {
        static private final List<Plugin> DEFAULTS = List.of(
                new Plugin("@hilla/generator-typescript-plugin-backbone"),
                new Plugin("@hilla/generator-typescript-plugin-client"),
                new Plugin("@hilla/generator-typescript-plugin-barrel"),
                new Plugin("@hilla/generator-typescript-plugin-model"),
                new Plugin("@hilla/generator-typescript-plugin-push"));

        PluginsProcessor() {
            super(DEFAULTS);
        }
    }
}
