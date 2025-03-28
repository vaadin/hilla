package com.vaadin.hilla.engine;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.utils.ConfigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParserConfiguration {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ParserConfiguration.class);
    private List<Class<? extends Annotation>> endpointAnnotations = List.of();
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of();
    private String openAPIBasePath;
    private Plugins plugins;

    public List<Class<? extends Annotation>> getEndpointAnnotations() {
        return endpointAnnotations;
    }

    public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return endpointExposedAnnotations;
    }

    public Optional<String> getOpenAPIBasePath() {
        return Optional.ofNullable(openAPIBasePath);
    }

    public Optional<Plugins> getPlugins() {
        return Optional.ofNullable(plugins);
    }

    public void setEndpointAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointAnnotations) {
        this.endpointAnnotations = endpointAnnotations;
    }

    public void setEndpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointExposedAnnotations) {
        this.endpointExposedAnnotations = endpointExposedAnnotations;
    }

    void setOpenAPIBasePath(String openAPIBasePath) {
        this.openAPIBasePath = openAPIBasePath;
    }

    void setPlugins(Plugins plugins) {
        this.plugins = plugins;
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

        public Plugin(String name, PluginConfiguration configuration) {
            this(name);
            this.configuration = configuration;
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

        @NonNull
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

        Plugins(@NonNull Collection<Plugin> use,
                @NonNull Collection<Plugin> disable,
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
        private static final List<Plugin> DEFAULTS;
        static {
            @SuppressWarnings("Intentionally, to avoid loading the class")
            Class kotlinNullabilityClass = null;
            Class kClass = null; // Just a class from kotlin-reflect library to
                                 // check if the library is available
            try {
                kotlinNullabilityClass = Class.forName(
                        "com.vaadin.hilla.parser.plugins.nonnull.kotlin.KotlinNullabilityPlugin");
                kClass = Class.forName("kotlin.reflect.KClass");
            } catch (Throwable e) {
                LOGGER.debug(
                        "Kotlin nullability plugin is not going to be loaded. "
                                + "If you with to have it enabled, please make sure the 'kotlin-reflect' "
                                + "and 'hilla-parser-jvm-plugin-nonnull-kotlin' libraries are included in your classpath.");
            }
            if (kotlinNullabilityClass != null && kClass != null) {
                DEFAULTS = List.of(new Plugin(BackbonePlugin.class.getName()),
                        new Plugin(MultipartFileCheckerPlugin.class.getName()),
                        new Plugin(TransferTypesPlugin.class.getName()),
                        new Plugin(kotlinNullabilityClass.getName()),
                        new Plugin(NonnullPlugin.class.getName()),
                        new Plugin(SubTypesPlugin.class.getName()),
                        new Plugin(ModelPlugin.class.getName()));
            } else {
                DEFAULTS = List.of(new Plugin(BackbonePlugin.class.getName()),
                        new Plugin(MultipartFileCheckerPlugin.class.getName()),
                        new Plugin(TransferTypesPlugin.class.getName()),
                        new Plugin(NonnullPlugin.class.getName()),
                        new Plugin(SubTypesPlugin.class.getName()),
                        new Plugin(ModelPlugin.class.getName()));
            }
        }

        PluginsProcessor() {
            super(DEFAULTS);
        }
    }
}
