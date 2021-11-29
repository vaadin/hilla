package com.vaadin.fusion.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.project.MavenProject;

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

        public Path resolveWithin(MavenProject project) {
            return Paths.get(project.getBasedir().getAbsolutePath(),
                    "node_modules", "@vaadin/" + name, "index.js");
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
