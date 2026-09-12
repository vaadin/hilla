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
package com.vaadin.hilla.generator.typescript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The folder the generated TypeScript is written to.
 *
 * <p>
 * The files of a run replace the ones of the run before it: what was generated
 * last time and is no longer is removed, along with the folders left empty by
 * it, while anything the folder holds which the generator did not write is left
 * alone. That is what the list of generated files is for, which is written
 * beside them.
 *
 * <p>
 * A file whose content has not changed is left untouched rather than written
 * again, so that watching the folder, as the development server does, only sees
 * what actually differs.
 */
public final class OutputFolder {
    /**
     * The file listing what the last run generated, which is how a later run
     * tells its own files from the ones of the application.
     */
    public static final String FILE_LIST = "generated-file-list.txt";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(OutputFolder.class);

    private final Path folder;

    public OutputFolder(Path folder) {
        this.folder = Objects.requireNonNull(folder);
    }

    /**
     * Writes the files of one generation, and removes what the run before it
     * left behind.
     *
     * <p>
     * A generation of nothing leaves no list behind either: with nothing
     * generated, nothing in the folder is the generator's any more.
     *
     * @param files
     *            the files of the generation, by their path relative to this
     *            folder
     */
    public void write(List<GeneratedFile> files) throws IOException {
        var previous = readFileList();

        if (files.isEmpty()) {
            remove(List.copyOf(previous));
            Files.deleteIfExists(folder.resolve(FILE_LIST));
            return;
        }

        Files.createDirectories(folder);
        Files.writeString(folder.resolve(FILE_LIST), files.stream()
                .map(file -> file.path() + "\n").collect(Collectors.joining()));

        var written = new LinkedHashSet<String>();

        for (var file : files) {
            write(file);
            written.add(file.path());
        }

        remove(previous.stream().filter(path -> !written.contains(path))
                .toList());
    }

    private void write(GeneratedFile file) throws IOException {
        var path = folder.resolve(file.path());

        if (Files.exists(path)
                && Files.readString(path).equals(file.content())) {
            LOGGER.debug("The generated file {} stayed the same", path);
            return;
        }

        Files.createDirectories(path.getParent());
        Files.writeString(path, file.content());
    }

    /**
     * Removes the given files of an earlier run, and every folder they leave
     * empty behind, which is where the types of a package removed since then
     * were written: the last file to go takes the folders above it along, as
     * far up as they are left empty.
     */
    private void remove(List<String> paths) throws IOException {
        for (var path : paths) {
            var file = folder.resolve(path);

            if (Files.deleteIfExists(file)) {
                LOGGER.debug("Removed the generated file {}", file);
                removeIfEmpty(file.getParent());
            }
        }
    }

    private void removeIfEmpty(Path directory) throws IOException {
        while (directory.startsWith(folder) && !directory.equals(folder)
                && isEmpty(directory)) {
            Files.delete(directory);
            LOGGER.debug("Removed the empty folder {}", directory);
            directory = directory.getParent();
        }
    }

    private static boolean isEmpty(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try (var entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }

    /**
     * What the run before this one generated, which is nothing if the folder
     * has never been generated into.
     */
    private Set<String> readFileList() throws IOException {
        var list = folder.resolve(FILE_LIST);

        if (!Files.isRegularFile(list)) {
            return Set.of();
        }

        try (var lines = Files.lines(list)) {
            return lines.filter(line -> !line.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
