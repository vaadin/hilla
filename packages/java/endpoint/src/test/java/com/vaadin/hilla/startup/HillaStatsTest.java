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
package com.vaadin.hilla.startup;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Platform;
import com.vaadin.flow.server.frontend.FrontendUtils;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@NotThreadSafe
public class HillaStatsTest {

    @Before
    @After
    public void cleanupVersions() throws Exception {
        UsageStatistics.resetEntries();
        final Field memoizedHillaVersionField = Platform.class
                .getDeclaredField("hillaVersion");
        memoizedHillaVersionField.setAccessible(true);
        memoizedHillaVersionField.set(null, null);
    }

    @Before
    public void setup() {

    }

    private void fakeHilla(boolean react) throws IOException {

    }

    @Test
    @Ignore("https://github.com/vaadin/hilla/issues/2129")
    public void when_hillaIsUsed_and_reactIsNotEnabled_LitIsReportedByDefault() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class);
                MockedStatic<FrontendUtils> mockedStaticFrontendUtils = mockStatic(
                        FrontendUtils.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                .thenReturn(Optional.of("24.4.0"));
            mockedStaticFrontendUtils
                    .when(() -> FrontendUtils.isHillaUsed(Mockito.any()))
                    .thenReturn(true);

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());
            HillaStats.report(deploymentConfiguration);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0", entries.get("hilla"));
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get("hilla+lit"));
            assertNull("entries: " + entries, entries.get("hilla+react"));
        }
    }

    private static Map<String, String> getEntries() {
        return UsageStatistics.getEntries()
                .collect(Collectors.toMap(UsageStatistics.UsageEntry::getName,
                        UsageStatistics.UsageEntry::getVersion));
    }

    @Test
    @Ignore("https://github.com/vaadin/hilla/issues/2129")
    public void when_hillaIsUsed_and_reactIsEnabled_ReactIsReportedProperly()
            throws Exception {
        Map<String, String> entries = getEntries();
        assertEquals("entries: " + entries, 1, entries.size());
        fakeHilla(true);
        // HillaStats.report();
        entries = getEntries();
        assertEquals("entries: " + entries, "2.1.1", entries.get("hilla"));
        assertEquals("entries: " + entries, "2.1.1",
                entries.get("hilla+react"));
        assertNull("entries: " + entries, entries.get("hilla+lit"));
    }
}
