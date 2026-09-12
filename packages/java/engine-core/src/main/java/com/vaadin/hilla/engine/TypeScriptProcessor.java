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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.generator.typescript.ClientWriter;
import com.vaadin.hilla.generator.typescript.GeneratedFile;
import com.vaadin.hilla.generator.typescript.OutputFolder;
import com.vaadin.hilla.generator.typescript.TypeScriptWriter;

/**
 * Writes the TypeScript of the endpoints, which is what the browser calls the
 * server through, into the folder the application reads it from.
 *
 * <p>
 * This is the Java side of what the Node generator does, which it is replacing
 * one step at a time.
 */
public final class TypeScriptProcessor {
    /**
     * The client the application may bring itself, next to the folder the
     * generated files go into. When it is there, the endpoints call the server
     * through it and no client is generated.
     */
    private static final String CUSTOM_CLIENT_FILE = "connect-client.ts";
    private static final String CUSTOM_CLIENT_MODULE = "../connect-client.js";

    private static final Logger logger = LoggerFactory
            .getLogger(TypeScriptProcessor.class);

    private final Path outputDirectory;

    public TypeScriptProcessor(EngineAutoConfiguration conf) {
        this(conf.getBaseDir(), conf.getOutputDir());
    }

    /**
     * @param baseDir
     *            the folder of the application, which a relative output folder
     *            is resolved against
     * @param outputDirectory
     *            the folder the generated files go into
     */
    public TypeScriptProcessor(Path baseDir, Path outputDirectory) {
        Objects.requireNonNull(baseDir);
        this.outputDirectory = Objects.requireNonNull(outputDirectory)
                .isAbsolute() ? outputDirectory
                        : baseDir.resolve(outputDirectory);
    }

    /**
     * Writes the files of one generation, and removes what the run before it
     * left behind.
     *
     * @param generation
     *            everything the parser found, as the writers need it
     */
    public void process(Generation generation) throws GeneratorException {
        logger.debug("Writing the TypeScript of {} endpoints into {}",
                generation.endpoints().size(), outputDirectory);

        try {
            new OutputFolder(outputDirectory).write(files(generation));
        } catch (IOException e) {
            throw new GeneratorException(
                    "Unable to write the generated TypeScript", e);
        }
    }

    /**
     * The files of a generation, which are none at all without a single browser
     * callable class: there is nothing to call then, and whatever was generated
     * for one before is no longer the application's.
     */
    private List<GeneratedFile> files(Generation generation) {
        return generation.endpoints().isEmpty() ? List.of()
                : new TypeScriptWriter(clientModule()).write(generation);
    }

    /**
     * The module the generated endpoints call the server through, which is the
     * client of the application when it has one of its own.
     */
    private String clientModule() {
        return Files.isRegularFile(
                outputDirectory.resolveSibling(CUSTOM_CLIENT_FILE))
                        ? CUSTOM_CLIENT_MODULE
                        : ClientWriter.MODULE_SPECIFIER;
    }
}
