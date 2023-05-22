package dev.hilla.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.model.ModelPlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.utils.ConfigList;

public final class ParserConfiguration {
    private String endpointAnnotation;
    private String endpointExposedAnnotation;
    private String openAPIBasePath;
    private Plugins plugins;
    private List<String> packages;

    public Optional<String> getEndpointAnnotation() {
        return Optional.ofNullable(endpointAnnotation);
    }

    public Optional<String> getEndpointExposedAnnotation() {
        return Optional.ofNullable(endpointExposedAnnotation);
    }

    public Optional<String> getOpenAPIBasePath() {
        return Optional.ofNullable(openAPIBasePath);
    }

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
        var that = (ParserConfiguration) o;
        return Objects.equals(endpointAnnotation, that.endpointAnnotation)
                && Objects.equals(endpointExposedAnnotation,
                        that.endpointExposedAnnotation)
                && Objects.equals(openAPIBasePath, that.openAPIBasePath)
                && Objects.equals(plugins, that.plugins)
                && Objects.equals(packages, that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointAnnotation, endpointExposedAnnotation,
                openAPIBasePath, plugins, packages);
    }

    public Optional<List<String>> getPackages() {
        return Optional.ofNullable(packages);
    }

    void setEndpointAnnotation(String endpointAnnotation) {
        this.endpointAnnotation = endpointAnnotation;
    }

    void setEndpointExposedAnnotation(String endpointExposedAnnotation) {
        this.endpointExposedAnnotation = endpointExposedAnnotation;
    }

    void setOpenAPIBasePath(String openAPIBasePath) {
        this.openAPIBasePath = openAPIBasePath;
    }

    void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public static class Plugin {
        private PluginConfiguration configuration;
        private String name;

        // Maven and Jackson require a constructor for deserialization
        public Plugin() {
        }

        Plugin(String name) {
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

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class Plugins implements ConfigList<Plugin> {
        private final List<Plugin> disable = new ArrayList<>();
        private final boolean disableAllDefaults;
        private final List<Plugin> use = new ArrayList<>();

        // Maven and Jackson require a constructor for deserialization
        public Plugins() {
            disableAllDefaults = false;
        }

        Plugins(@Nonnull Collection<Plugin> use,
                @Nonnull Collection<Plugin> disable,
                boolean disableAllDefaults) {
            this.disable.addAll(disable);
            this.use.addAll(use);
            this.disableAllDefaults = disableAllDefaults;
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
    }

    static class PluginsProcessor extends ConfigList.Processor<Plugin> {
        private static final List<Plugin> DEFAULTS = List.of(
                new Plugin(BackbonePlugin.class.getName()),
                new Plugin(TransferTypesPlugin.class.getName()),
                new Plugin(NonnullPlugin.class.getName()),
                new Plugin(ModelPlugin.class.getName()));

        PluginsProcessor() {
            super(DEFAULTS);
        }
    }
}
