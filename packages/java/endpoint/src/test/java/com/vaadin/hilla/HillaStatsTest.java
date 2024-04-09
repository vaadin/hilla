/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Platform;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

@NotThreadSafe
public class HillaStatsTest {
    private ClassLoader oldContextClassLoader;

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Before
    public void rememberContextClassLoader() throws Exception {
        oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        fakeHilla(false);
    }

    @Before
    @After
    public void cleanupVersions() throws Exception {
        UsageStatistics.resetEntries();
        final Field memoizedHillaVersionField = Platform.class
                .getDeclaredField("hillaVersion");
        memoizedHillaVersionField.setAccessible(true);
        memoizedHillaVersionField.set(null, null);
    }

    @After
    public void restoreContextClassLoader() {
        Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    }

    private URL fakeJar(String artifactId, String version) throws IOException {
        final Path jar = temporary.newFolder().toPath();
        final Path pomProperties = jar
                .resolve("META-INF/maven/com.vaadin.hilla/" + artifactId
                        + "/pom.properties");
        Files.createDirectories(pomProperties.getParent());
        Files.writeString(pomProperties, "version=" + version);
        return jar.toUri().toURL();
    }

    private void fakeHilla(boolean react) throws IOException {
        final LinkedList<URL> classpath = new LinkedList<>();
        classpath.add(fakeJar("hilla", "2.1.1"));
        if (react) {
            classpath.add(fakeJar("hilla-react", "2.1.1"));
        }
        final ClassLoader classLoader = new URLClassLoader(
                classpath.toArray(new URL[0]), null);
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    @Ignore("https://github.com/vaadin/hilla/issues/2129")
    public void testLitIsReportedByDefault() {
        Map<String, String> entries = getEntries();
        assertEquals("entries: " + entries, 1, entries.size());
        HillaStats.report();
        entries = getEntries();
        assertEquals("entries: " + entries, "2.1.1", entries.get("hilla"));
        assertEquals("entries: " + entries, "2.1.1", entries.get("hilla+lit"));
        assertNull("entries: " + entries, entries.get("hilla+react"));
    }

    private static Map<String, String> getEntries() {
        return UsageStatistics.getEntries()
                .collect(Collectors.toMap(UsageStatistics.UsageEntry::getName,
                        UsageStatistics.UsageEntry::getVersion));
    }

    @Test
    @Ignore("https://github.com/vaadin/hilla/issues/2129")
    public void testReactIsReportedProperly() throws Exception {
        Map<String, String> entries = getEntries();
        assertEquals("entries: " + entries, 1, entries.size());
        fakeHilla(true);
        HillaStats.report();
        entries = getEntries();
        assertEquals("entries: " + entries, "2.1.1", entries.get("hilla"));
        assertEquals("entries: " + entries, "2.1.1",
                entries.get("hilla+react"));
        assertNull("entries: " + entries, entries.get("hilla+lit"));
    }
}
