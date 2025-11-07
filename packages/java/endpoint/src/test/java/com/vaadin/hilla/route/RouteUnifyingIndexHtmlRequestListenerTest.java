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
package com.vaadin.hilla.route;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;

import com.sun.security.auth.UserPrincipal;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import tools.jackson.databind.ObjectMapper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouteParameterData;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.internal.menu.MenuRegistry;

import static com.vaadin.flow.internal.menu.MenuRegistry.FILE_ROUTES_JSON_NAME;
import static com.vaadin.flow.internal.menu.MenuRegistry.FILE_ROUTES_JSON_PROD_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

public class RouteUnifyingIndexHtmlRequestListenerTest {

    protected static final String SCRIPT_STRING = RouteUnifyingIndexHtmlRequestListener.SCRIPT_STRING
            .replace("%s;", "");

    private RouteUnifyingIndexHtmlRequestListener requestListener;
    private IndexHtmlResponse indexHtmlResponse;
    private VaadinService vaadinService;
    private VaadinRequest vaadinRequest;
    private DeploymentConfiguration deploymentConfiguration;
    private File productionRouteFile;
    private MenuAccessControl menuAccessControl;

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    private ServerAndClientViewsProvider serverClientViewsProvider;

    @Before
    public void setUp() throws IOException {
        MenuRegistry.clearFileRoutesCache();
        vaadinService = Mockito.mock(VaadinService.class);

        VaadinContext vaadinContext = Mockito.mock(VaadinContext.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(vaadinContext.getAttribute(Lookup.class))
                .thenReturn(lookup);

        Mockito.when(vaadinService.getContext()).thenReturn(vaadinContext);

        deploymentConfiguration = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);
        serverClientViewsProvider = new ServerAndClientViewsProvider(
                deploymentConfiguration, null, true);
        requestListener = new RouteUnifyingIndexHtmlRequestListener(
                serverClientViewsProvider);

        indexHtmlResponse = Mockito.mock(IndexHtmlResponse.class);
        vaadinRequest = Mockito.mock(VaadinRequest.class);
        Mockito.when(indexHtmlResponse.getVaadinRequest())
                .thenReturn(vaadinRequest);
        var userPrincipal = Mockito.mock(Principal.class);
        Mockito.when(vaadinRequest.getUserPrincipal())
                .thenReturn(userPrincipal);

        Mockito.when(vaadinRequest.getService()).thenReturn(vaadinService);
        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);

        final Document document = Mockito.mock(Document.class);
        final Element element = new Element("head");
        Mockito.when(document.head()).thenReturn(element);
        Mockito.when(indexHtmlResponse.getDocument()).thenReturn(document);

        final RouteRegistry serverRouteRegistry = Mockito
                .mock(RouteRegistry.class);
        final List<RouteData> flowRegisteredRoutes = prepareServerRoutes();
        Mockito.when(serverRouteRegistry.getRegisteredRoutes())
                .thenReturn(flowRegisteredRoutes);
        Mockito.when(serverRouteRegistry
                .getRegisteredAccessibleMenuRoutes(any(), any()))
                .thenReturn(flowRegisteredRoutes);

        final Router router = Mockito.mock(Router.class);
        Mockito.when(vaadinService.getRouter()).thenReturn(router);
        Mockito.when(router.getRegistry()).thenReturn(serverRouteRegistry);

        var instantiator = Mockito.mock(Instantiator.class);
        menuAccessControl = Mockito.mock(MenuAccessControl.class);
        Mockito.when(vaadinService.getInstantiator()).thenReturn(instantiator);
        Mockito.when(instantiator.getMenuAccessControl())
                .thenReturn(menuAccessControl);
        Mockito.when(menuAccessControl.getPopulateClientSideMenu())
                .thenReturn(MenuAccessControl.PopulateClientMenu.ALWAYS);
        Mockito.doCallRealMethod().when(menuAccessControl)
                .canAccessView(any(AvailableViewInfo.class));

        // Add test data for production mode
        projectRoot.newFolder("META-INF", "VAADIN");
        productionRouteFile = new File(projectRoot.getRoot(),
                FILE_ROUTES_JSON_PROD_PATH);

        copyClientRoutes("clientRoutes.json", productionRouteFile);

        CurrentInstance.set(VaadinRequest.class, vaadinRequest);
    }

    @After
    public void tearDown() {
        CurrentInstance.set(VaadinRequest.class, null);
    }

    private static List<RouteData> prepareServerRoutes() {
        final List<RouteData> flowRegisteredRoutes = new ArrayList<>();
        final RouteData bar = new RouteData(Collections.emptyList(), "bar",
                Collections.emptyList(), Component.class,
                Collections.emptyList());
        flowRegisteredRoutes.add(bar);

        final RouteData foo = new RouteData(Collections.emptyList(), "foo",
                Collections.emptyList(), RouteTarget.class,
                Collections.emptyList());
        flowRegisteredRoutes.add(foo);

        final RouteData wildcard = new RouteData(Collections.emptyList(),
                "wildcard/:___wildcard*",
                Map.of("___wildcard",
                        new RouteParameterData(":___wildcard*", null)),
                RouteTarget.class, Collections.emptyList());
        flowRegisteredRoutes.add(wildcard);

        final RouteData editUser = new RouteData(Collections.emptyList(),
                "/:___userId/edit",
                Map.of("___userId", new RouteParameterData(":___userId", null)),
                RouteTarget.class, Collections.emptyList());
        flowRegisteredRoutes.add(editUser);

        final RouteData comments = new RouteData(Collections.emptyList(),
                "comments/:___commentId?",
                Map.of("___commentId",
                        new RouteParameterData(":___commentId?", null)),
                RouteTarget.class, Collections.emptyList());
        flowRegisteredRoutes.add(comments);
        return flowRegisteredRoutes;
    }

    @Test
    public void when_productionMode_anonymous_user_should_modifyIndexHtmlResponse_with_anonymously_allowed_routes()
            throws IOException {
        testProductionModeWithRoles(null, null,
                "/META-INF/VAADIN/available-views-anonymous.json");
    }

    @Test
    public void when_productionMode_authenticated_user_should_modifyIndexHtmlResponse_with_user_allowed_routes()
            throws IOException {
        testProductionModeWithRoles(true, "ROLE_USER",
                "/META-INF/VAADIN/available-views-user.json");
    }

    @Test
    public void when_productionMode_admin_user_should_modifyIndexHtmlResponse_with_anonymous_and_admin_allowed_routes()
            throws IOException {
        testProductionModeWithRoles(true, "ROLE_ADMIN",
                "/META-INF/VAADIN/available-views-admin.json", true);
    }

    @Test
    public void when_developmentMode_should_modifyIndexHtmlResponse()
            throws IOException {
        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                    .thenReturn(true);
            mockDevelopmentMode();
            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        verifyAndAssertViews("/META-INF/VAADIN/available-views-admin.json",
                true);
    }

    @Test
    public void should_collectServerViews() {
        final Map<String, AvailableViewInfo> views;

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            views = serverClientViewsProvider.collectServerViews(true);
        }
        MatcherAssert.assertThat(views, Matchers.aMapWithSize(4));
        MatcherAssert.assertThat(views.get("/bar").title(),
                Matchers.is("Component"));
        MatcherAssert.assertThat(views.get("/foo").title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(views.get("/bar").route(),
                Matchers.is("/bar"));
        MatcherAssert.assertThat(views.get("/wildcard").route(),
                Matchers.is("/wildcard"));
        MatcherAssert.assertThat(views.get("/comments").route(),
                Matchers.is("/comments"));
    }

    @Test
    public void when_productionMode_should_collectClientViews()
            throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(true);
        Mockito.when(vaadinRequest.getUserPrincipal())
                .thenReturn(Mockito.mock(UserPrincipal.class));
        Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                .thenReturn(true);

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class);
                MockedStatic<MenuRegistry> menuRegistry = Mockito
                        .mockStatic(MenuRegistry.class, CALLS_REAL_METHODS)) {
            ClassLoader mockClassLoader = Mockito.mock(ClassLoader.class);

            Mockito.when(
                    mockClassLoader.getResource(FILE_ROUTES_JSON_PROD_PATH))
                    .thenReturn(productionRouteFile.toURI().toURL());

            menuRegistry.when(() -> MenuRegistry.getClassLoader())
                    .thenReturn(mockClassLoader);
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            var views = serverClientViewsProvider
                    .collectClientViews(vaadinRequest);
            MatcherAssert.assertThat(views, Matchers.aMapWithSize(4));
        }
    }

    @Test
    public void when_developmentMode_should_collectClientViews()
            throws IOException {
        mockDevelopmentMode();
        Mockito.when(vaadinRequest.getUserPrincipal())
                .thenReturn(Mockito.mock(UserPrincipal.class));
        Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                .thenReturn(true);

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            var views = serverClientViewsProvider
                    .collectClientViews(vaadinRequest);
            MatcherAssert.assertThat(views, Matchers.aMapWithSize(4));
        }
    }

    @Test
    public void when_exposeServerRoutesToClient_false_serverSideRoutesAreNotInResponse()
            throws IOException {
        testWithCustomViewsProvider(false, null,
                "/META-INF/VAADIN/only-client-views.json");
    }

    @Test
    public void when_exposeServerRoutesToClient_noLayout_serverSideRoutesAreNotInResponse()
            throws IOException {
        testWithCustomViewsProvider(true,
                MenuAccessControl.PopulateClientMenu.AUTOMATIC,
                "/META-INF/VAADIN/only-client-views.json", true, false);
    }

    @Test
    public void when_exposeServerRoutesToClient_layoutExists_serverSideRoutesAreInResponse()
            throws IOException {
        assertServerRoutesExposedToClientWhenLayoutExists(
                "clientRoutesWithLayout.json", "server-and-client-views.json");
    }

    @Test
    public void when_exposeServerRoutesToClient_layoutExists_routeWithEmptyPath_serverSideRoutesAreInResponse()
            throws IOException {
        assertServerRoutesExposedToClientWhenLayoutExists(
                "clientRoutesWithLayoutAndIndexView.json",
                "server-and-client-views-layout-and-index-route.json");
    }

    private void assertServerRoutesExposedToClientWhenLayoutExists(
            String testJsonFile, String expectedJsonFile) throws IOException {
        // Use routes with layout
        copyClientRoutes(testJsonFile, productionRouteFile);

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class);
                MockedStatic<MenuRegistry> menuRegistry = Mockito
                        .mockStatic(MenuRegistry.class, CALLS_REAL_METHODS)) {
            ClassLoader mockClassLoader = Mockito.mock(ClassLoader.class);

            Mockito.when(menuAccessControl.getPopulateClientSideMenu())
                    .thenReturn(MenuAccessControl.PopulateClientMenu.AUTOMATIC);
            Mockito.when(
                    mockClassLoader.getResource(FILE_ROUTES_JSON_PROD_PATH))
                    .thenReturn(productionRouteFile.toURI().toURL());

            menuRegistry.when(() -> MenuRegistry.getClassLoader())
                    .thenReturn(mockClassLoader);
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            Mockito.when(deploymentConfiguration.isProductionMode())
                    .thenReturn(true);
            Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                    .thenReturn(true);
            var serverAndClientViewsProvider = new ServerAndClientViewsProvider(
                    deploymentConfiguration, null, true);
            var requestListener = new RouteUnifyingIndexHtmlRequestListener(
                    serverAndClientViewsProvider);

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());
        MatcherAssert.assertThat(
                "No data nodes for script tag", indexHtmlResponse.getDocument()
                        .head().select("script").dataNodes().isEmpty(),
                Matchers.is(false));

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());
        final String cleanViews = removeTrailingSemicolon(views);

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(cleanViews);
        var expected = mapper.readTree(getClass()
                .getResourceAsStream("/META-INF/VAADIN/" + expectedJsonFile));

        MatcherAssert.assertThat("Different amount of items", actual.size(),
                Matchers.is(expected.size()));

        Iterator<String> elementsFields = expected.propertyNames().iterator();
        while (elementsFields.hasNext()) {
            String field = elementsFields.next();
            MatcherAssert.assertThat("Generated missing fieldName " + field,
                    actual.has(field), Matchers.is(true));
            MatcherAssert.assertThat("Missing element " + field,
                    actual.get(field), Matchers.equalTo(expected.get(field)));
        }
    }

    private void mockDevelopmentMode() throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(false);
        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(deploymentConfiguration.getFrontendFolder())
                .thenReturn(frontendGeneratedDir.getParentFile());

        File clientFiles = new File(frontendGeneratedDir,
                FILE_ROUTES_JSON_NAME);
        copyClientRoutes("clientRoutes.json", clientFiles);
    }

    private void copyClientRoutes(String jsonFile, File clientFiles)
            throws IOException {
        try (InputStream routes = RouteUnifyingIndexHtmlRequestListenerTest.class
                .getResourceAsStream(jsonFile)) {
            FileUtils.copyInputStreamToFile(routes, clientFiles);
        }
    }

    private void testProductionModeWithRoles(Boolean hasUserPrincipal,
            String role, String expectedJsonPath) throws IOException {
        testProductionModeWithRoles(hasUserPrincipal, role, expectedJsonPath,
                false);
    }

    private void testProductionModeWithRoles(Boolean hasUserPrincipal,
            String role, String expectedJsonPath, boolean checkSize)
            throws IOException {
        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class);
                MockedStatic<MenuRegistry> menuRegistry = Mockito
                        .mockStatic(MenuRegistry.class, CALLS_REAL_METHODS)) {
            ClassLoader mockClassLoader = Mockito.mock(ClassLoader.class);

            Mockito.when(
                    mockClassLoader.getResource(FILE_ROUTES_JSON_PROD_PATH))
                    .thenReturn(productionRouteFile.toURI().toURL());

            menuRegistry.when(() -> MenuRegistry.getClassLoader())
                    .thenReturn(mockClassLoader);
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            Mockito.when(deploymentConfiguration.isProductionMode())
                    .thenReturn(true);

            if (hasUserPrincipal == null) {
                Mockito.when(vaadinRequest.getUserPrincipal()).thenReturn(null);
            } else if (role != null) {
                Mockito.when(vaadinRequest.isUserInRole(role)).thenReturn(true);
            }

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        verifyAndAssertViews(expectedJsonPath, checkSize);
    }

    private void verifyAndAssertViews(String expectedJsonPath,
            boolean checkSize) throws IOException {
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        verifyAndAssertViewsWithoutDocumentVerification(expectedJsonPath,
                checkSize);
    }

    private void verifyAndAssertViewsWithoutDocumentVerification(
            String expectedJsonPath, boolean checkSize) throws IOException {
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());
        final String cleanViews = removeTrailingSemicolon(views);

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(cleanViews);
        var expected = mapper
                .readTree(getClass().getResourceAsStream(expectedJsonPath));

        if (checkSize) {
            MatcherAssert.assertThat("Different amount of items", actual.size(),
                    Matchers.is(expected.size()));
        }

        Iterator<String> elementsFields = expected.propertyNames().iterator();
        while (elementsFields.hasNext()) {
            String field = elementsFields.next();
            MatcherAssert.assertThat("Generated missing fieldName " + field,
                    actual.has(field), Matchers.is(true));
            MatcherAssert.assertThat("Missing element " + field,
                    actual.get(field), Matchers.equalTo(expected.get(field)));
        }
    }

    private void testWithCustomViewsProvider(boolean exposeServerRoutes,
            MenuAccessControl.PopulateClientMenu populateClientMenu,
            String expectedJsonPath) throws IOException {
        testWithCustomViewsProvider(exposeServerRoutes, populateClientMenu,
                expectedJsonPath, true, true);
    }

    private void testWithCustomViewsProvider(boolean exposeServerRoutes,
            MenuAccessControl.PopulateClientMenu populateClientMenu,
            String expectedJsonPath, boolean checkSize, boolean checkFields)
            throws IOException {
        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class);
                MockedStatic<MenuRegistry> menuRegistry = Mockito
                        .mockStatic(MenuRegistry.class, CALLS_REAL_METHODS)) {
            ClassLoader mockClassLoader = Mockito.mock(ClassLoader.class);

            if (populateClientMenu != null) {
                Mockito.when(menuAccessControl.getPopulateClientSideMenu())
                        .thenReturn(populateClientMenu);
            }

            Mockito.when(
                    mockClassLoader.getResource(FILE_ROUTES_JSON_PROD_PATH))
                    .thenReturn(productionRouteFile.toURI().toURL());

            menuRegistry.when(() -> MenuRegistry.getClassLoader())
                    .thenReturn(mockClassLoader);
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            Mockito.when(deploymentConfiguration.isProductionMode())
                    .thenReturn(true);
            Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                    .thenReturn(true);
            var serverClientViewsProvider = new ServerAndClientViewsProvider(
                    deploymentConfiguration, null, exposeServerRoutes);
            var requestListener = new RouteUnifyingIndexHtmlRequestListener(
                    serverClientViewsProvider);

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }

        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());
        MatcherAssert.assertThat(
                "No data nodes for script tag", indexHtmlResponse.getDocument()
                        .head().select("script").dataNodes().isEmpty(),
                Matchers.is(false));

        if (checkFields) {
            verifyAndAssertViewsWithoutDocumentVerification(expectedJsonPath,
                    checkSize);
        } else {
            DataNode script = indexHtmlResponse.getDocument().head()
                    .select("script").dataNodes().get(0);

            final String scriptText = script.getWholeData();
            MatcherAssert.assertThat(scriptText,
                    Matchers.startsWith(SCRIPT_STRING));

            final String views = scriptText.substring(SCRIPT_STRING.length());
            // Remove trailing semicolon if present - Jackson 3 is stricter
            final String cleanViews = views.endsWith(";")
                    ? views.substring(0, views.length() - 1)
                    : views;

            final var mapper = new ObjectMapper();

            var actual = mapper.readTree(cleanViews);
            var expected = mapper
                    .readTree(getClass().getResourceAsStream(expectedJsonPath));

            MatcherAssert.assertThat("Different amount of items", actual.size(),
                    Matchers.is(expected.size()));
        }
    }

    private String removeTrailingSemicolon(String views) {
        // Jackson 3 is stricter about trailing content
        return views.endsWith(";") ? views.substring(0, views.length() - 1)
                : views;
    }

    @PageTitle("RouteTarget")
    private static class RouteTarget extends Component {
    }

}
