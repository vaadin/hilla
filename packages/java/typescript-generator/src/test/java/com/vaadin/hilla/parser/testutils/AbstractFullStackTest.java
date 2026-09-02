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
package com.vaadin.hilla.parser.testutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for tests which verify that a set of browser callable Java classes
 * produces the expected TypeScript files.
 *
 * <p>
 * The expected files are stored as plain TypeScript files in a
 * {@code snapshots} folder next to the test resources of the test class. Both
 * the content and the location of every generated file are verified: a
 * generated file without a snapshot fails the test, and so does a snapshot
 * without a generated file.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * public class MyEndpointTest extends AbstractFullStackTest {
 *     &#64;Test
 *     public void should_GenerateCorrectTypeScript() {
 *         assertTypescriptMatchesSnapshot(MyEndpoint.class);
 *     }
 * }
 * </pre>
 *
 * <p>
 * The generated client file is the same for every endpoint, so it is left out
 * of the comparison unless a test asks for it with
 * {@link FullStackGenerator#withClientFile()}.
 *
 * <p>
 * Most cases need no test class at all: {@code EndpointGenerationTest}
 * discovers every package owning a snapshots folder and generates from the
 * browser callable classes it contains. Extend this class only for a case which
 * needs more, such as a configured plugin, and put it in the package of the
 * case, which excludes the package from the discovered ones. The snapshots
 * folder belongs to the package rather than to the test class, so there can be
 * only one such test class per package.
 *
 * <p>
 * Snapshots can be recreated from the current generator output by running the
 * tests with {@code -Dhilla.test.updateSnapshots}. Always review the resulting
 * diff: it is the specification of what the generator produces.
 */
public abstract class AbstractFullStackTest {
    static final String SNAPSHOTS_DIR = "snapshots";

    /**
     * The generated client, which is the same for every endpoint and is
     * therefore only compared by the tests which are about it.
     *
     * @see FullStackGenerator#withClientFile()
     */
    private static final String CLIENT_FILE = "connect-client.default.ts";

    private static final String UPDATE_SNAPSHOTS_PROPERTY = "hilla.test.updateSnapshots";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractFullStackTest.class);

    /**
     * Generates TypeScript for the given browser callable classes and verifies
     * it against the snapshots of this test.
     *
     * @param endpointClasses
     *            the browser callable classes to generate from
     */
    protected void assertTypescriptMatchesSnapshot(
            Class<?>... endpointClasses) {
        assertTypescriptMatchesSnapshot(generator(endpointClasses));
    }

    /**
     * Verifies the output of a customized generator against the snapshots of
     * this test.
     *
     * @param generator
     *            the generator, as returned by {@link #generator(Class...)}
     */
    protected void assertTypescriptMatchesSnapshot(
            FullStackGenerator generator) {
        var generated = new LinkedHashMap<>(generator.generate());

        if (!generator.isClientFileIncluded()) {
            generated.remove(CLIENT_FILE);
        }

        if (isUpdatingSnapshots()) {
            updateSnapshots(generator, generated);
        }

        try {
            new TypeScriptComparator().compare(readSnapshots(generator),
                    generated);
        } catch (AssertionError e) {
            throw new AssertionError(
                    getSnapshotsDir(generator) + ": " + e.getMessage(), e);
        }
    }

    /**
     * Creates a generator for the given browser callable classes, which can be
     * customized before being passed to
     * {@link #assertTypescriptMatchesSnapshot(FullStackGenerator)}.
     *
     * @param endpointClasses
     *            the browser callable classes to generate from
     */
    protected FullStackGenerator generator(Class<?>... endpointClasses) {
        return new FullStackGenerator(getClass(), endpointClasses);
    }

    private static boolean isUpdatingSnapshots() {
        var value = System.getProperty(UPDATE_SNAPSHOTS_PROPERTY);
        return value != null && !"false".equalsIgnoreCase(value);
    }

    private static Map<String, String> readSnapshots(
            FullStackGenerator generator) {
        var snapshotsDir = getSnapshotsDir(generator);

        if (!Files.isDirectory(snapshotsDir)) {
            throw new AssertionError("The snapshots folder " + snapshotsDir
                    + " does not exist. Run the tests with -D"
                    + UPDATE_SNAPSHOTS_PROPERTY + " to create it.");
        }

        try (var files = Files.walk(snapshotsDir)) {
            var snapshots = new LinkedHashMap<String, String>();

            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".ts")).sorted()
                    .forEach(path -> {
                        try {
                            snapshots.put(relativize(snapshotsDir, path),
                                    Files.readString(path));
                        } catch (IOException e) {
                            throw new IllegalStateException(
                                    "Unable to read the snapshot " + path, e);
                        }
                    });

            return snapshots;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read the snapshots in " + snapshotsDir, e);
        }
    }

    private static void updateSnapshots(FullStackGenerator generator,
            Map<String, String> generated) {
        var snapshotsDir = getSnapshotsDir(generator);
        LOGGER.info("Updating the snapshots in {}", snapshotsDir);

        try {
            deleteExistingSnapshots(snapshotsDir);

            for (var entry : generated.entrySet()) {
                var path = snapshotsDir.resolve(entry.getKey());
                Files.createDirectories(path.getParent());
                Files.writeString(path, entry.getValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to update the snapshots in " + snapshotsDir, e);
        }
    }

    /**
     * Removes the snapshots of a previous run, but nothing else: the folder may
     * also contain files which are not snapshots.
     */
    private static void deleteExistingSnapshots(Path snapshotsDir)
            throws IOException {
        if (!Files.isDirectory(snapshotsDir)) {
            return;
        }

        try (var files = Files.walk(snapshotsDir)) {
            for (var path : files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".ts")).toList()) {
                Files.delete(path);
            }
        }

        // Snapshots are generated into a folder structure which mirrors the
        // package of the entity, so leftover folders have to go as well
        try (var files = Files.walk(snapshotsDir)) {
            for (var path : files.sorted(Comparator.reverseOrder()).toList()) {
                if (!path.equals(snapshotsDir) && Files.isDirectory(path)
                        && isEmpty(path)) {
                    Files.delete(path);
                }
            }
        }
    }

    private static boolean isEmpty(Path dir) throws IOException {
        try (var entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        }
    }

    /**
     * The snapshots are read from and written to the sources rather than to the
     * copy in the build folder, so that updated snapshots are not lost on the
     * next build.
     */
    private static Path getSnapshotsDir(FullStackGenerator generator) {
        return generator.getModuleDir()
                .resolve(Path.of("src", "test", "resources"))
                .resolve(generator.getSnapshotsPackage().replace('.', '/'))
                .resolve(SNAPSHOTS_DIR);
    }

    private static String relativize(Path dir, Path path) {
        return dir.relativize(path).toString().replace('\\', '/');
    }
}
