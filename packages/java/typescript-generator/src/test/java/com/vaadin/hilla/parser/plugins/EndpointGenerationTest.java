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
package com.vaadin.hilla.parser.plugins;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;
import com.vaadin.hilla.parser.testutils.ResourceLoader;

/**
 * Verifies the TypeScript generated for every test case which needs nothing
 * beyond the browser callable classes themselves.
 *
 * <p>
 * A test case is a package owning a {@code snapshots} folder in the test
 * resources: the browser callable classes of that package and of its
 * subpackages are generated together, and the result is compared against the
 * snapshots. Adding a case is therefore a matter of adding a package with an
 * endpoint in it and running the tests once with
 * {@code -Dhilla.test.updateSnapshots}; no test class is involved.
 *
 * <p>
 * A case which needs more than that, such as a configured plugin or an
 * assertion which is not about the generated files, has its own test class
 * extending {@link AbstractFullStackTest} in its package. Such packages are
 * skipped here, so that every case is covered exactly once.
 */
public class EndpointGenerationTest extends AbstractFullStackTest {
    private static final String ROOT_PACKAGE = "com.vaadin.hilla.parser";
    private static final String ENDPOINT_ANNOTATION = ROOT_PACKAGE
            + ".testutils.annotations.Endpoint";
    private static final String SNAPSHOTS_DIR = "snapshots";

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void should_GenerateTheExpectedTypeScript(String testCase,
            List<Class<?>> endpoints) {
        assertTypescriptMatchesSnapshot(
                generator(endpoints.toArray(Class<?>[]::new))
                        .withSnapshotsIn(testCase));
    }

    static Stream<Arguments> should_GenerateTheExpectedTypeScript() {
        var testCases = findTestCases();

        try (var scan = scan()) {
            var packagesWithTestClass = packagesWithTestClass(scan);
            var endpoints = scan.getClassesWithAnnotation(ENDPOINT_ANNOTATION)
                    .loadClasses();

            return testCases.stream().filter(
                    testCase -> !packagesWithTestClass.contains(testCase))
                    .map(testCase -> Arguments.of(testCase,
                            endpointsOf(testCase, testCases, endpoints)))
                    .toList().stream();
        }
    }

    /**
     * Guards against a case which is covered neither here nor by a test class
     * of its own, which is what happens when a package with an endpoint is
     * added without generating its snapshots.
     */
    @Test
    public void should_CoverEveryBrowserCallableClass() {
        var testCases = findTestCases();

        try (var scan = scan()) {
            var packagesWithTestClass = packagesWithTestClass(scan);

            var uncovered = scan.getClassesWithAnnotation(ENDPOINT_ANNOTATION)
                    .getNames().stream()
                    // The parser core has tests of its own, which are not
                    // about the generated TypeScript
                    .filter(name -> isInside(name,
                            EndpointGenerationTest.class.getPackageName()))
                    .filter(name -> Stream
                            .concat(testCases.stream(),
                                    packagesWithTestClass.stream())
                            .noneMatch(pkg -> isInside(name, pkg)))
                    .sorted().toList();

            assertTrue(uncovered.isEmpty(),
                    () -> "There is neither a snapshots folder nor a test class"
                            + " covering " + uncovered);
        }
    }

    private static ScanResult scan() {
        return new ClassGraph().enableAnnotationInfo().enableClassInfo()
                .acceptPackages(ROOT_PACKAGE).scan();
    }

    private static List<String> packagesWithTestClass(ScanResult scan) {
        return scan.getSubclasses(AbstractFullStackTest.class).getNames()
                .stream()
                .filter(name -> !name
                        .equals(EndpointGenerationTest.class.getName()))
                .map(name -> name.substring(0, name.lastIndexOf('.'))).toList();
    }

    /**
     * Finds the packages owning a snapshots folder, which are the test cases.
     */
    private static List<String> findTestCases() {
        var resources = moduleDir()
                .resolve(Path.of("src", "test", "resources"));
        var root = resources.resolve(ROOT_PACKAGE.replace('.', '/'));

        try (var paths = Files.walk(root)) {
            return paths.filter(Files::isDirectory)
                    .filter(path -> SNAPSHOTS_DIR
                            .equals(path.getFileName().toString()))
                    .map(path -> resources.relativize(path.getParent())
                            .toString().replace(java.io.File.separatorChar, '.')
                            .replace('/', '.'))
                    .sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to look for test cases in " + root, e);
        }
    }

    /**
     * Collects the browser callable classes of a test case: the ones of its
     * package and of its subpackages, except those belonging to a nested test
     * case.
     */
    private static List<Class<?>> endpointsOf(String testCase,
            List<String> testCases, List<Class<?>> endpoints) {
        var nested = testCases.stream()
                .filter(other -> isInside(other, testCase)).toList();

        return endpoints.stream()
                .filter(endpoint -> isInside(endpoint.getName(), testCase))
                .filter(endpoint -> nested.stream().noneMatch(
                        other -> isInside(endpoint.getName(), other)))
                .sorted(Comparator.comparing(Class::getName)).toList();
    }

    private static boolean isInside(String name, String packageName) {
        return name.startsWith(packageName + ".");
    }

    private static Path moduleDir() {
        try {
            return new ResourceLoader(EndpointGenerationTest.class)
                    .findTargetDirPath().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to find the module folder",
                    e);
        }
    }
}
