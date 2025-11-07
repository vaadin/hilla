/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.engine;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.tools.MainClassFinder;
import tools.jackson.databind.ObjectMapper;

/**
 * Finds browser callables (endpoints) in a non-running Hilla application, using
 * Spring AOT to detect available beans and select those who are annotated.
 */
public class AotBrowserCallableFinder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AotBrowserCallableFinder.class);
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String SPRING_AOT_PROCESSOR = "org.springframework.boot.SpringApplicationAotProcessor";

    private static final String METADATA_FILE_NAME = "reachability-metadata.json";

    /**
     * Finds the classes annotated with the endpoint annotations.
     *
     * @param engineConfiguration
     *            the engine configuration
     * @return the list of classes annotated with the endpoint annotations
     * @throws BrowserCallableFinderException
     *             if an error occurs while finding the browser callables
     */
    public static List<Class<?>> find(
            EngineAutoConfiguration engineConfiguration)
            throws BrowserCallableFinderException {
        try {
            // Determine the main application class
            var applicationClass = determineApplicationClass(
                    engineConfiguration);
            if (applicationClass == null) {
                throw new BrowserCallableFinderException(
                        "Application has no main class");
            }

            // Generate the AOT artifacts, including reachability metadata JSON
            var reflectConfigPath = generateAotArtifacts(engineConfiguration,
                    applicationClass);

            // Load annotated classes from reachability metadata JSON
            return loadAnnotatedClasses(engineConfiguration, reflectConfigPath);
        } catch (Exception e) {
            if (e instanceof BrowserCallableFinderException bce) {
                throw bce;
            }
            throw new BrowserCallableFinderException(e);
        }
    }

    private static String determineApplicationClass(
            EngineAutoConfiguration engineConfiguration) {
        var mainClass = engineConfiguration.getMainClass();
        if (mainClass != null) {
            return mainClass;
        }
        try {
            mainClass = engineConfiguration.getClassesDirs().stream()
                    .map(path -> {
                        try {
                            return MainClassFinder.findSingleMainClass(
                                    path.toFile(),
                                    SPRING_BOOT_APPLICATION_CLASS_NAME);
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).findFirst().orElse(null);
            if (mainClass == null) {
                LOGGER.debug(
                        "This project has not been recognized as a Spring Boot"
                                + " application because a main class could not be found.");
            }
            return mainClass;
        } catch (NoClassDefFoundError e) {
            LOGGER.debug(
                    "Spring Boot org.springframework.boot.loader.tools.MainClassFinder class not found. "
                            + "Can happen when a Maven project is configured to use com.vaadin:flow-maven-plugin instead of com.vaadin:vaadin-maven-plugin, "
                            + "for example projects using Vaadin Multiplatform Runtime. "
                            + "If Hilla is not a project requirement exclude it from the dependency tree, "
                            + "otherwise consider replacing com.vaadin:flow-maven-plugin with com.vaadin:hilla-maven-plugin.");
            return null;
        }
    }

    private static Path generateAotArtifacts(
            EngineAutoConfiguration engineConfiguration,
            String applicationClass) throws IOException, InterruptedException,
            BrowserCallableFinderException {
        var aotOutput = engineConfiguration.getBuildDir()
                .resolve("spring-aot/main");
        var classesDirectory = aotOutput.resolve("classes");
        var classpath = engineConfiguration.getClasspath().stream()
                .filter(Files::exists).toList();

        var settings = Stream.of("-cp",
                classpath.stream().map(AotBrowserCallableFinder::quotePath)
                        .collect(Collectors.joining(File.pathSeparator)),
                SPRING_AOT_PROCESSOR, applicationClass,
                quotePath(aotOutput.resolve("sources")),
                quotePath(aotOutput.resolve("resources")),
                quotePath(classesDirectory), engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId()).toList();

        var argsFile = engineConfiguration.getBuildDir()
                .resolve("hilla-aot-args.txt");
        Files.write(argsFile, settings);
        var report = engineConfiguration.getBuildDir()
                .resolve("hilla-aot-report.txt");
        var javaExecutable = ProcessHandle.current().info().command()
                .orElse(Path.of(System.getProperty("java.home"), "bin", "java")
                        .toString());

        // Runs the SpringApplicationAotProcessor to generate the
        // reflect-config.json file. This comes from the `process-aot` goal.
        var process = new ProcessBuilder().inheritIO()
                .command(javaExecutable, "@" + argsFile)
                .redirectOutput(report.toFile()).redirectErrorStream(true)
                .start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            LOGGER.debug(SPRING_AOT_PROCESSOR + " exited with code " + exitCode
                    + ". The output of the process is available in " + report);
        }

        var json = aotOutput.resolve(Path.of("resources", "META-INF",
                "native-image", engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId(), METADATA_FILE_NAME));

        if (!Files.isRegularFile(json)) {
            throw new BrowserCallableFinderException(String.format(
                    "The `%s` tool has not produced the expected"
                            + " `%s` file, which is used to"
                            + " identify available endpoints.",
                    SPRING_AOT_PROCESSOR, METADATA_FILE_NAME));
        }

        return json;
    }

    private static List<Class<?>> loadAnnotatedClasses(
            EngineAutoConfiguration engineConfiguration, Path reflectConfigPath)
            throws IOException, BrowserCallableFinderException {
        // The file simply contains a list of beans, we just need their names,
        // which are class names.
        var jsonContent = Files.readString(reflectConfigPath);
        var objectMapper = new ObjectMapper();
        var rootNode = objectMapper.readTree(jsonContent);

        if (!rootNode.isObject()) {
            throwUnknownMetadataFormat("root node is not an object");
        }

        var reflectionsNode = rootNode.get("reflection");
        if (reflectionsNode == null) {
            throwUnknownMetadataFormat("\"reflection\" key is null");
        }

        if (!reflectionsNode.isArray()) {
            throwUnknownMetadataFormat("\"reflection\" key is not an array");
        }

        // Extract candidate class names
        var candidates = new ArrayList<String>();
        for (var node : reflectionsNode) {
            var typeNode = node.get("type");
            if (typeNode.isString()) {
                String type = node.get("type").asString();
                candidates.add(type);
            } else {
                LOGGER.trace("Ignoring non-string type for property {}",
                        typeNode);
            }
        }

        var annotationNames = engineConfiguration.getEndpointAnnotations()
                .stream().map(Class::getName).toList();
        var classLoader = engineConfiguration.getClassLoader();

        var result = candidates.stream().map(type -> {
            try {
                return Class.forName(type, false, classLoader);
            } catch (Throwable t) {
                LOGGER.debug("Failed to load class {}: {}", type,
                        t.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).filter(cls -> {
            var annotations = Arrays.stream(cls.getAnnotations())
                    .map(Annotation::annotationType).map(Class::getName)
                    .toList();
            return annotations.stream().anyMatch(annotationNames::contains);
        }).collect(Collectors.toList());

        return (List<Class<?>>) (List<?>) result;
    }

    private static String quotePath(Path path) {
        return '"' + path.toString().replace("\\", "\\\\") + '"';
    }

    private static void throwUnknownMetadataFormat(String reason)
            throws BrowserCallableFinderException {
        throw new BrowserCallableFinderException(String.format(
                "Unable to read information about beans from the AOT metadata output file `%s`: %s",
                METADATA_FILE_NAME, reason));
    }
}
