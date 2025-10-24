package com.vaadin.hilla.engine;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.typescript.parser.core.Parser;

public final class GeneratorProcessor {
    public static String GENERATED_FILE_LIST_NAME = "generated-file-list.txt";

    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorProcessor.class);

    private final Path baseDir;
    private final Set<Path> classPath;
    private final Path outputDirectory;
    private List<Class<? extends Annotation>> endpointAnnotations;
    private List<Class<? extends Annotation>> endpointExposedAnnotations;

    public GeneratorProcessor(EngineAutoConfiguration conf) {
        this.baseDir = conf.getBaseDir();
        this.classPath = conf.getClasspath();
        this.outputDirectory = conf.getOutputDir();
        this.endpointAnnotations = conf.getEndpointAnnotations();
        this.endpointExposedAnnotations = conf.getEndpointExposedAnnotations();
    }

    public void process(@NonNull List<Class<?>> browserCallables)
            throws GeneratorException {
        if (browserCallables.isEmpty()) {
            logger.debug("No browser callables found, cleaning up old files");
            cleanup();
            return;
        }

        logger.debug("Generating TypeScript for {} browser callables",
                browserCallables.size());

        try {
            var parser = new Parser()
                    .classPath(classPath.stream().map(Path::toString)
                            .collect(Collectors.toSet()))
                    .endpointAnnotations(endpointAnnotations)
                    .endpointExposedAnnotations(endpointExposedAnnotations);

            var outputDir = outputDirectory.isAbsolute() ? outputDirectory
                    : baseDir.resolve(outputDirectory);

            Files.createDirectories(outputDir);

            parser.generateTypeScript(browserCallables, outputDir);

            logger.debug("TypeScript generation completed successfully");
        } catch (IOException e) {
            throw new GeneratorException(
                    "Failed to generate TypeScript files", e);
        }
    }

    private void cleanup() throws GeneratorException {
        var generatedFilesListFile = outputDirectory
                .resolve(GENERATED_FILE_LIST_NAME);
        if (!generatedFilesListFile.toFile().exists()) {
            logger.debug(
                    "Generated file list file does not exist, skipping cleanup.");
            return;
        }

        logger.debug("Cleaning up old output.");
        var generatedFilesList = List.<String> of();
        try {
            generatedFilesList = Files.readAllLines(generatedFilesListFile);
        } catch (IOException e) {
            throw new GeneratorException(
                    "Unable to read generated file list file", e);
        }

        try {
            for (var line : generatedFilesList) {
                var path = outputDirectory.resolve(line);
                logger.debug("Removing generated file: {}", path);
                Files.deleteIfExists(path);
                // Also remove any empty parent directories
                var dir = path.getParent();
                while (dir.startsWith(outputDirectory)
                        && !dir.equals(outputDirectory)
                        && Files.isDirectory(dir) && Objects.requireNonNull(
                                dir.toFile().list()).length == 0) {
                    logger.debug("Removing unused generated directory: {}",
                            dir);
                    Files.deleteIfExists(dir);
                }
            }
        } catch (IOException e) {
            throw new GeneratorException("Unable to cleanup generated files",
                    e);
        }

        try {
            Files.deleteIfExists(generatedFilesListFile);
        } catch (IOException e) {
            throw new GeneratorException(
                    "Unable to remove the generated file list file", e);
        }
    }

}
