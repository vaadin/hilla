/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package dev.hilla.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FallibleCommand;

import dev.hilla.engine.ConfigurationException;
import dev.hilla.engine.EngineConfiguration;

import jakarta.annotation.Nonnull;

/**
 * Abstract class for endpoint related generators.
 */
abstract class AbstractTaskEndpointGenerator implements FallibleCommand {
    private final String buildDirectoryName;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final File outputDirectory;
    private final File projectDirectory;
    protected final ClassLoader classLoader;
    private EngineConfiguration engineConfiguration;

    AbstractTaskEndpointGenerator(@Nonnull File projectDirectory,
            @Nonnull String buildDirectoryName, @Nonnull File outputDirectory,
            @Nonnull ClassLoader classLoader) {
        this.projectDirectory = Objects.requireNonNull(projectDirectory,
                "Project directory cannot be null");
        this.buildDirectoryName = Objects.requireNonNull(buildDirectoryName,
                "Build directory name cannot be null");
        this.outputDirectory = Objects.requireNonNull(outputDirectory,
                "Output directory name cannot be null");
        this.classLoader = Objects.requireNonNull(classLoader,
                "ClassLoader cannot not be null");
    }

    protected EngineConfiguration getEngineConfiguration()
            throws ExecutionFailedException {
        if (engineConfiguration == null) {
            prepareEngineConfiguration();
        }

        return engineConfiguration;
    }

    protected void prepareEngineConfiguration()
            throws ExecutionFailedException {
        EngineConfiguration config = null;

        var configDir = projectDirectory.toPath().resolve(buildDirectoryName);

        try {
            config = EngineConfiguration.loadDirectory(configDir);
        } catch (IOException | ConfigurationException e) {
            logger.warn(
                    "Hilla engine configuration found, but not read correctly",
                    e);
        }

        if (config == null) {
            logger.info(
                    "Hilla engine configuration not found: configure using build system plugin");

            try {
                var reader = new MavenXpp3Reader();
                var model = reader.read(
                        new FileReader(new File(projectDirectory, "pom.xml")));
                var plugins = model.getBuild().getPlugins();
                config = plugins.stream()
                        .filter(p -> p.getGroupId().equals("dev.hilla") && p
                                .getArtifactId().equals("hilla-maven-plugin"))
                        .findFirst()
                        .map(plugin -> getPluginConfiguration(plugin,
                                EngineConfiguration.class))
                        .orElse(new EngineConfiguration());
            } catch (IOException | XmlPullParserException e) {
                throw new ExecutionFailedException(
                        "Failed to read Hilla engine configuration", e);
            }
        }

        var buildPath = new File(projectDirectory, buildDirectoryName).toPath();
        var javaClassPath = System.getProperty("java.class.path");
        var pathSeparator = System.getProperty("path.separator");
        var classPath = List.of(javaClassPath.split(pathSeparator));
        config = new EngineConfiguration.Builder(config)
                .baseDir(projectDirectory.toPath()).buildDir(buildPath)
                .classesDir(buildPath.resolve("classes"))
                .outputDir(outputDirectory.toPath()).classPath(classPath)
                .create();

        this.engineConfiguration = config;
    }

    // TODO: handle exceptions correctly
    public static <T> T getPluginConfiguration(Plugin plugin, Class<T> type,
            String... children) {
        // Get the configuration of the plugin.
        var config = (Xpp3Dom) plugin.getConfiguration();
        if (config == null) {
            return null;
        }

        // Get the child element of the configuration.
        for (var child : children) {
            config = config.getChild(child);
            if (config == null) {
                return null;
            }
        }

        // Create a new instance of the specified class T.
        T instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to create instance of " + type.getSimpleName(), e);
        }

        // Create a PlexusConfiguration object from the Xpp3Dom object.
        var configuration = new XmlPlexusConfiguration(config);

        var classWorld = new ClassWorld("hillaRealm", type.getClassLoader());
        var classRealm = new ClassRealm(classWorld, "hillaRealm",
                type.getClassLoader());

        // Create a new instance of the ComponentConfigurator and configure it.
        var configurator = new BasicComponentConfigurator();

        try {
            configurator.configureComponent(instance, configuration,
                    classRealm);

            return instance;
        } catch (ComponentConfigurationException ex) {
            throw new RuntimeException(
                    "Failed to apply configuration to instance", ex);
        }
    }
}
