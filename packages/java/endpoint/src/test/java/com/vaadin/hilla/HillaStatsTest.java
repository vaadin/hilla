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

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.Platform;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.frontend.FrontendUtils;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.vaadin.hilla.HillaStats.ENDPOINT_ACTIVE;
import static com.vaadin.hilla.HillaStats.HAS_ENDPOINT;
import static com.vaadin.hilla.HillaStats.HAS_HILLA_CUSTOM_ROUTE;
import static com.vaadin.hilla.HillaStats.HAS_HILLA_FS_ROUTE;
import static com.vaadin.hilla.HillaStats.HAS_HYBRID_ROUTING;
import static com.vaadin.hilla.HillaStats.HAS_LIT;
import static com.vaadin.hilla.HillaStats.HAS_REACT;
import static com.vaadin.hilla.HillaStats.HAS_REACT_LIT;
import static com.vaadin.hilla.HillaStats.HILLA_USAGE;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@NotThreadSafe
public class HillaStatsTest {

    @Rule
    public TemporaryFolder frontendFolder = new TemporaryFolder();

    @Before
    @After
    public void cleanupVersions() throws Exception {
        UsageStatistics.resetEntries();
        final Field memoizedHillaVersionField = Platform.class
                .getDeclaredField("hillaVersion");
        memoizedHillaVersionField.setAccessible(true);
        memoizedHillaVersionField.set(null, null);
    }

    @Test
    public void when_hillaIsNotUsed_noRenderingLibraryUsageIsReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class);
                MockedStatic<FrontendUtils> mockedStaticFrontendUtils = mockStatic(
                        FrontendUtils.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            mockedStaticFrontendUtils
                    .when(() -> FrontendUtils.isHillaUsed(Mockito.any()))
                    .thenReturn(false);

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_LIT));
            assertNull("entries: " + entries, entries.get(HAS_REACT));
            assertNull("entries: " + entries, entries.get(HAS_REACT_LIT));
        }
    }

    @Test
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
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0", entries.get(HAS_LIT));
            assertNull("entries: " + entries, entries.get(HAS_REACT));
            assertNull("entries: " + entries, entries.get(HAS_REACT_LIT));
        }
    }

    @Test
    public void when_hillaIsUsed_and_reactIsEnabled_and_reactRouterIsRequired_ReactIsReportedProperly() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class);
                MockedStatic<FrontendUtils> mockedStaticFrontendUtils = mockStatic(
                        FrontendUtils.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            mockedStaticFrontendUtils
                    .when(() -> FrontendUtils.isHillaUsed(Mockito.any()))
                    .thenReturn(true);
            mockedStaticFrontendUtils.when(
                    () -> FrontendUtils.isReactRouterRequired(Mockito.any()))
                    .thenReturn(true);

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.isReactEnabled()).thenReturn(true);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_REACT));
            assertNull("entries: " + entries, entries.get(HAS_LIT));
            assertNull("entries: " + entries, entries.get(HAS_REACT_LIT));
        }
    }

    @Test
    public void when_hillaIsUsed_and_reactIsEnabled_and_reactRouterIsNotRequired_ReactIsNotReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class);
                MockedStatic<FrontendUtils> mockedStaticFrontendUtils = mockStatic(
                        FrontendUtils.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            mockedStaticFrontendUtils
                    .when(() -> FrontendUtils.isHillaUsed(Mockito.any()))
                    .thenReturn(true);
            mockedStaticFrontendUtils.when(
                    () -> FrontendUtils.isReactRouterRequired(Mockito.any()))
                    .thenReturn(false);

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.isReactEnabled()).thenReturn(true);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_REACT));
            assertNull("entries: " + entries, entries.get(HAS_LIT));
            assertNull("entries: " + entries, entries.get(HAS_REACT_LIT));
        }
    }

    @Test
    public void when_hillaIsUsed_and_reactIsNotEnabled_and_reactRouterIsRequired_ReactIsNotReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class);
                MockedStatic<FrontendUtils> mockedStaticFrontendUtils = mockStatic(
                        FrontendUtils.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            mockedStaticFrontendUtils
                    .when(() -> FrontendUtils.isHillaUsed(Mockito.any()))
                    .thenReturn(true);
            mockedStaticFrontendUtils.when(
                    () -> FrontendUtils.isReactRouterRequired(Mockito.any()))
                    .thenReturn(true);

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_REACT));
            assertNull("entries: " + entries, entries.get(HAS_LIT));
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_REACT_LIT));
        }
    }

    @Test
    public void when_hasHillaFsRouteIsTrue_hasHillaFsRoute_markedAsUSed() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            var vaadinService = Mockito.mock(VaadinService.class);

            Map<String, String> entries = getEntries();
            assertEquals("entries: " + entries, 1, entries.size());

            HillaStats.reportGenericHasFeatures(vaadinService, true);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_HILLA_FS_ROUTE));
        }
    }

    @Test
    public void when_routesTsxExistsInFrontend_hasHillaCustomRoute_isReported()
            throws IOException {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());

            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries,
                    entries.get(HAS_HILLA_CUSTOM_ROUTE));

            frontendFolder.newFile("routes.tsx");
            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_HILLA_CUSTOM_ROUTE));
        }
    }

    @Test
    public void when_routesTsExistsInFrontend_hasHillaCustomRoute_isReported()
            throws IOException {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries,
                    entries.get(HAS_HILLA_CUSTOM_ROUTE));

            frontendFolder.newFile("routes.ts");
            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_HILLA_CUSTOM_ROUTE));
        }
    }

    @Test
    public void when_routeRegistryIsNotEmpty_and_hasHillaFsRouteIsTrue_hasHybridRouting_isReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            HillaStats.reportGenericHasFeatures(vaadinService, true);
            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_HYBRID_ROUTING));

            var router = Mockito.mock(Router.class);
            when(vaadinService.getRouter()).thenReturn(router);
            var routeRegistry = Mockito.mock(RouteRegistry.class);
            when(router.getRegistry()).thenReturn(routeRegistry);
            var routeDataList = List.of(new RouteData(Collections.emptyList(),
                    "foo", Collections.emptyList(), null,
                    Collections.emptyList()));
            when(routeRegistry.getRegisteredRoutes()).thenReturn(routeDataList);

            HillaStats.reportGenericHasFeatures(vaadinService, true);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_HYBRID_ROUTING));
        }
    }

    @Test
    public void when_routeRegistryIsNotEmpty_and_hasHillaCustomRouteIsTrue_hasHybridRouting_isReported()
            throws IOException {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_HYBRID_ROUTING));

            frontendFolder.newFile("routes.tsx");

            var router = Mockito.mock(Router.class);
            when(vaadinService.getRouter()).thenReturn(router);
            var routeRegistry = Mockito.mock(RouteRegistry.class);
            when(router.getRegistry()).thenReturn(routeRegistry);
            var routeDataList = List.of(new RouteData(Collections.emptyList(),
                    "foo", Collections.emptyList(), null,
                    Collections.emptyList()));
            when(routeRegistry.getRegisteredRoutes()).thenReturn(routeDataList);

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_HYBRID_ROUTING));
        }
    }

    @Test
    public void when_routeRegistryIsEmpty_and_hasHillaFsRouteIsTrue_hasHybridRouting_isNotReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            var router = Mockito.mock(Router.class);
            when(vaadinService.getRouter()).thenReturn(router);
            var routeRegistry = Mockito.mock(RouteRegistry.class);
            when(router.getRegistry()).thenReturn(routeRegistry);
            when(routeRegistry.getRegisteredRoutes())
                    .thenReturn(Collections.emptyList());

            HillaStats.reportGenericHasFeatures(vaadinService, true);
            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries, entries.get(HAS_HYBRID_ROUTING));
        }
    }

    @Test
    public void when_hasHillaFsRoute_isTrue_hillaUsage_isReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries, entries.get(HILLA_USAGE));

            HillaStats.reportGenericHasFeatures(vaadinService, true);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HILLA_USAGE));
        }
    }

    @Test
    public void when_hasHillaCustomRoute_isTrue_hillaUsage_isReported()
            throws IOException {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));

            DeploymentConfiguration deploymentConfiguration = Mockito
                    .mock(DeploymentConfiguration.class);
            when(deploymentConfiguration.getFrontendFolder())
                    .thenReturn(frontendFolder.getRoot());
            frontendFolder.newFile("routes.tsx");
            when(deploymentConfiguration.isReactEnabled()).thenReturn(false);
            var vaadinService = Mockito.mock(VaadinService.class);
            when(vaadinService.getDeploymentConfiguration())
                    .thenReturn(deploymentConfiguration);

            Map<String, String> entries = getEntries();
            assertNull("entries: " + entries, entries.get(HILLA_USAGE));

            HillaStats.reportGenericHasFeatures(vaadinService, false);
            entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HILLA_USAGE));
        }
    }

    @Test
    public void when_reportHasEndpoint_isCalled_hasEndpoint_isReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            HillaStats.reportHasEndpoint();
            var entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(HAS_ENDPOINT));
        }
    }

    @Test
    public void when_reportEndpointActive_isCalled_endpointActive_isReported() {
        try (MockedStatic<Platform> mockedStaticPlatform = mockStatic(
                Platform.class)) {
            mockedStaticPlatform.when(Platform::getHillaVersion)
                    .thenReturn(Optional.of("24.4.0"));
            HillaStats.reportEndpointActive();
            var entries = getEntries();
            assertEquals("entries: " + entries, "24.4.0",
                    entries.get(ENDPOINT_ACTIVE));
        }
    }

    private static Map<String, String> getEntries() {
        return UsageStatistics.getEntries()
                .collect(Collectors.toMap(UsageStatistics.UsageEntry::getName,
                        UsageStatistics.UsageEntry::getVersion));
    }
}
