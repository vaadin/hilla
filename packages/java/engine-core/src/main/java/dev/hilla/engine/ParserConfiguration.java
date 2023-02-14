package dev.hilla.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.model.ModelPlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.utils.ConfigList;

public final class ParserConfiguration {
    private ParserClassPathConfiguration classPath;
    private String endpointAnnotation;
    private String endpointExposedAnnotation;
    private String openAPIPath;
    private Plugins plugins;

    public Optional<ParserClassPathConfiguration> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public Optional<String> getEndpointAnnotation() {
        return Optional.ofNullable(endpointAnnotation);
    }

    public Optional<String> getEndpointExposedAnnotation() {
        return Optional.ofNullable(endpointExposedAnnotation);
    }

    public Optional<String> getOpenAPIPath() {
        return Optional.ofNullable(openAPIPath);
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
        return Objects.equals(classPath, that.classPath)
                && Objects.equals(endpointAnnotation, that.endpointAnnotation)
                && Objects.equals(endpointExposedAnnotation,
                        that.endpointExposedAnnotation)
                && Objects.equals(openAPIPath, that.openAPIPath)
                && Objects.equals(plugins, that.plugins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classPath, endpointAnnotation,
                endpointExposedAnnotation, openAPIPath, plugins);
    }

    void setClassPath(ParserClassPathConfiguration classPath) {
        this.classPath = classPath;
    }

    public void setEndpointAnnotation(String endpointAnnotation) {
        this.endpointAnnotation = endpointAnnotation;
    }

    public void setEndpointExposedAnnotation(String endpointExposedAnnotation) {
        this.endpointExposedAnnotation = endpointExposedAnnotation;
    }

    void setOpenAPIPath(String openAPIPath) {
        this.openAPIPath = openAPIPath;
    }

    void setPlugins(Plugins plugins) {
        this.plugins = plugins;
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

    public static class Plugins implements ConfigList<Plugin> {
        private final Set<Plugin> disable = new HashSet<>();
        private final boolean disableAllDefaults = false;
        private final Set<Plugin> use = new HashSet<>();

        public Plugins() {
        }

        public Plugins(@Nonnull Collection<Plugin> use,
                @Nonnull Collection<Plugin> disable) {
            this.disable.addAll(disable);
            this.use.addAll(use);
        }

        @Override
        public Set<Plugin> getDisabledOptions() {
            return disable;
        }

        @Override
        public Set<Plugin> getUsedOptions() {
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
        private static final Set<Plugin> defaults = Set.of(
                new Plugin(TransferTypesPlugin.class.getName()),
                new Plugin(BackbonePlugin.class.getName()),
                new Plugin(NonnullPlugin.class.getName()),
                new Plugin(ModelPlugin.class.getName()));

        public PluginsProcessor() {
            super(defaults);
        }
    }
}
