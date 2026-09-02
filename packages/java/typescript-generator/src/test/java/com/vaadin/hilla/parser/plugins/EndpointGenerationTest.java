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
 * A test case is the outermost package holding browser callable classes: those
 * classes, together with the ones of its subpackages, are generated and the
 * result is compared against the {@code snapshots} folder of the package in the
 * test resources. Adding a case is therefore a matter of adding a package with
 * an endpoint in it and running the tests once with
 * {@code -Dhilla.test.updateSnapshots}, which creates the snapshots; no test
 * class is involved.
 *
 * <p>
 * A case which needs more than that, such as a configured plugin or an
 * assertion which is not about the generated files, has its own test class
 * extending {@link AbstractFullStackTest} in its package. Such a package is
 * skipped here, and its classes are left out of the surrounding case, so that
 * every endpoint is covered exactly once.
 */
public class EndpointGenerationTest extends AbstractFullStackTest {
    private static final String ROOT_PACKAGE = EndpointGenerationTest.class
            .getPackageName();
    private static final String ENDPOINT_ANNOTATION = "com.vaadin.hilla.parser.testutils.annotations.Endpoint";
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
        try (var scan = scan()) {
            var endpoints = endpoints(scan);
            var withTestClass = packagesWithTestClass(scan);

            return findTestCases(endpoints).stream()
                    .filter(testCase -> !withTestClass.contains(testCase))
                    .map(testCase -> Arguments.of(testCase,
                            endpointsOf(testCase, endpoints, withTestClass)))
                    .toList().stream();
        }
    }

    /**
     * Guards against a snapshots folder which is no longer the expected output
     * of anything, which is what is left behind when the endpoints of a case
     * are removed or moved.
     */
    @Test
    public void should_NotHaveStaleSnapshots() {
        try (var scan = scan()) {
            var testCases = findTestCases(endpoints(scan));

            var stale = findSnapshotPackages().stream()
                    .filter(pkg -> !testCases.contains(pkg)).sorted().toList();

            assertTrue(stale.isEmpty(),
                    () -> "There is no browser callable class left to generate"
                            + " the snapshots of " + stale);
        }
    }

    private static ScanResult scan() {
        return new ClassGraph().enableAnnotationInfo().enableClassInfo()
                .acceptPackages(ROOT_PACKAGE).scan();
    }

    private static List<Class<?>> endpoints(ScanResult scan) {
        return scan.getClassesWithAnnotation(ENDPOINT_ANNOTATION).loadClasses();
    }

    /**
     * The packages which have a test class of their own, matched exactly: a
     * test class covers its own package, never a whole subtree.
     */
    private static List<String> packagesWithTestClass(ScanResult scan) {
        return scan.getSubclasses(AbstractFullStackTest.class).getNames()
                .stream()
                .filter(name -> !name
                        .equals(EndpointGenerationTest.class.getName()))
                .map(EndpointGenerationTest::packageOf).distinct().toList();
    }

    /**
     * Finds the test cases, which are the packages holding browser callable
     * classes and having no such package above them: a case covers its own
     * subpackages, which is what lets an endpoint refer to entities, or to
     * other endpoints, kept next to it.
     */
    private static List<String> findTestCases(List<Class<?>> endpoints) {
        var packages = endpoints.stream()
                .map(endpoint -> packageOf(endpoint.getName())).distinct()
                .sorted().toList();

        return packages.stream().filter(pkg -> packages.stream()
                .noneMatch(other -> isInside(pkg, other))).toList();
    }

    /**
     * Collects the browser callable classes of a test case: the ones of its
     * package and of its subpackages, except those covered by a test class of
     * their own.
     */
    private static List<Class<?>> endpointsOf(String testCase,
            List<Class<?>> endpoints, List<String> packagesWithTestClass) {
        var covered = packagesWithTestClass.stream()
                .filter(pkg -> isInside(pkg, testCase)).toList();

        return endpoints.stream()
                .filter(endpoint -> isInside(endpoint.getName(), testCase))
                .filter(endpoint -> covered.stream()
                        .noneMatch(pkg -> isInside(endpoint.getName(), pkg)))
                .sorted(Comparator.comparing(Class::getName)).toList();
    }

    /**
     * Finds the packages owning a snapshots folder in the test resources.
     */
    private static List<String> findSnapshotPackages() {
        var resources = moduleDir()
                .resolve(Path.of("src", "test", "resources"));
        var root = resources.resolve(ROOT_PACKAGE.replace('.', '/'));

        try (var paths = Files.walk(root)) {
            return paths.filter(Files::isDirectory).filter(
                    path -> SNAPSHOTS_DIR.equals(path.getFileName().toString()))
                    .map(path -> resources.relativize(path.getParent())
                            .toString().replace(java.io.File.separatorChar, '.')
                            .replace('/', '.'))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to look for snapshots in " + root, e);
        }
    }

    /**
     * Whether the given class or package is the given package or below it.
     */
    private static boolean isInside(String name, String packageName) {
        return name.startsWith(packageName + ".");
    }

    private static String packageOf(String className) {
        return className.substring(0, className.lastIndexOf('.'));
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
