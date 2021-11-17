/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FrontendUtils;

/**
 * Wrapper for a generator directory that can do directory cleaning from stale
 * files and empty directories.
 */
class GenerationOutputDirectory {
    private final Logger logger = LoggerFactory
            .getLogger(GenerationOutputDirectory.class.getName());
    private final File outputDirectory;
    private final GeneratorFileVisitor visitor = new GeneratorFileVisitor(
            logger);

    GenerationOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String toString() {
        return outputDirectory.toString();
    }

    void clean() {
        clean(Collections.emptySet());
    }

    void clean(Set<File> files) {
        if (!outputDirectory.exists()) {
            return;
        }

        visitor.setGeneratedFiles(files);

        try {
            Files.walkFileTree(outputDirectory.toPath(), visitor);
        } catch (IOException e) {
            logger.info(String.format(
                    "Failed to access folder '%s' while cleaning generated sources.",
                    outputDirectory.toPath().toAbsolutePath()), e);
        }
    }

    Path toPath() {
        return outputDirectory.toPath();
    }

    static class GeneratorFileVisitor extends SimpleFileVisitor<Path> {
        private static final List<String> filesToKeep = Arrays.asList(
                ClientAPIGenerator.CONNECT_CLIENT_NAME,
                FrontendUtils.BOOTSTRAP_FILE_NAME,
                FrontendUtils.VITE_DEVMODE_TS, FrontendUtils.INDEX_TS,
                FrontendUtils.INDEX_JS, FrontendUtils.THEME_IMPORTS_NAME,
                FrontendUtils.THEME_IMPORTS_D_TS_NAME);
        private final Logger logger;
        private Set<File> generatedFiles;

        GeneratorFileVisitor(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path path,
                final IOException exc) {
            try (DirectoryStream<Path> stream = Files
                    .newDirectoryStream(path)) {
                if (!stream.iterator().hasNext()) {
                    remove(path);
                }
            } catch (IOException e) {
                logger.info(String.format(
                        "Failed to access folder '%s' while cleaning generated sources.",
                        path.toAbsolutePath()), e);
            }

            return FileVisitResult.CONTINUE;
        }

        public void setGeneratedFiles(final Set<File> generatedFiles) {
            this.generatedFiles = generatedFiles;
        }

        @Override
        public FileVisitResult visitFile(final Path path,
                final BasicFileAttributes attr) {
            File file = path.toFile();

            if (generatedFiles.contains(file)) {
                return FileVisitResult.CONTINUE;
            }

            final String fileName = file.getName();

            if (filesToKeep.contains(fileName)
                    || fileName.endsWith(".generated.js")) {
                return FileVisitResult.CONTINUE;
            }

            remove(path);

            return FileVisitResult.CONTINUE;
        }

        private void remove(Path path) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                logger.info(String.format(
                        "Failed to remove '%s' while cleaning the generated folder.",
                        path.toAbsolutePath()), e);
            }
        }
    }
}
