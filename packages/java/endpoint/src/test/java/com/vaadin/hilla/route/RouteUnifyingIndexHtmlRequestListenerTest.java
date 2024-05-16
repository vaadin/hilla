package com.vaadin.hilla.route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;

import com.sun.security.auth.UserPrincipal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import com.vaadin.flow.server.menu.MenuRegistry;
import com.vaadin.flow.server.menu.RouteParamType;

import static com.vaadin.flow.server.menu.MenuRegistry.FILE_ROUTES_JSON_NAME;
import static com.vaadin.flow.server.menu.MenuRegistry.FILE_ROUTES_JSON_PROD_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouteUnifyingIndexHtmlRequestListenerTest {

    protected static final String SCRIPT_STRING = RouteUnifyingIndexHtmlRequestListener.SCRIPT_STRING
            .replace("%s;", "");

    private final ClientRouteRegistry clientRouteRegistry = Mockito
            .mock(ClientRouteRegistry.class);
    private RouteUnifyingIndexHtmlRequestListener requestListener;
    private IndexHtmlResponse indexHtmlResponse;
    private VaadinService vaadinService;
    private VaadinRequest vaadinRequest;
    private DeploymentConfiguration deploymentConfiguration;
    private RouteUtil routeUtil;
    private File productionRouteFile;

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        vaadinService = Mockito.mock(VaadinService.class);

        VaadinContext vaadinContext = Mockito.mock(VaadinContext.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        when(vaadinContext.getAttribute(Lookup.class)).thenReturn(lookup);

        Mockito.when(vaadinService.getContext()).thenReturn(vaadinContext);

        deploymentConfiguration = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);
        routeUtil = new RouteUtil(clientRouteRegistry);
        requestListener = new RouteUnifyingIndexHtmlRequestListener(
                clientRouteRegistry, deploymentConfiguration, routeUtil, null,
                null, true);

        indexHtmlResponse = Mockito.mock(IndexHtmlResponse.class);
        vaadinRequest = Mockito.mock(VaadinRequest.class);
        Mockito.when(indexHtmlResponse.getVaadinRequest())
                .thenReturn(vaadinRequest);
        var userPrincipal = Mockito.mock(Principal.class);
        Mockito.when(vaadinRequest.getUserPrincipal())
                .thenReturn(userPrincipal);

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

        var instantiator = mock(Instantiator.class);
        var menuAccessControl = mock(MenuAccessControl.class);
        when(vaadinService.getInstantiator()).thenReturn(instantiator);
        when(instantiator.getMenuAccessControl()).thenReturn(menuAccessControl);
        when(menuAccessControl.getPopulateClientSideMenu())
                .thenReturn(MenuAccessControl.PopulateClientMenu.ALWAYS);

        // Add test data for production mode
        projectRoot.newFolder("META-INF", "VAADIN");
        productionRouteFile = new File(projectRoot.getRoot(),
                FILE_ROUTES_JSON_PROD_PATH);
        Files.writeString(productionRouteFile.toPath(), clientRoutes);
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
            Mockito.when(vaadinRequest.getUserPrincipal()).thenReturn(null);

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(views);
        var expected = mapper.readTree(getClass().getResource(
                "/META-INF/VAADIN/available-views-anonymous.json"));

        MatcherAssert.assertThat(actual, Matchers.is(expected));
    }

    @Test
    public void when_productionMode_authenticated_user_should_modifyIndexHtmlResponse_with_user_allowed_routes()
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
            Mockito.when(vaadinRequest.isUserInRole("ROLE_USER"))
                    .thenReturn(true);

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(views);
        var expected = mapper.readTree(getClass()
                .getResource("/META-INF/VAADIN/available-views-user.json"));

        MatcherAssert.assertThat(actual, Matchers.is(expected));
    }

    @Test
    public void when_productionMode_admin_user_should_modifyIndexHtmlResponse_with_anonymous_and_admin_allowed_routes()
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
            Mockito.when(vaadinRequest.isUserInRole("ROLE_ADMIN"))
                    .thenReturn(true);
            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(views);
        var expected = mapper.readTree(getClass()
                .getResource("/META-INF/VAADIN/available-views-admin.json"));

        MatcherAssert.assertThat(actual, Matchers.is(expected));
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
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText.substring(SCRIPT_STRING.length());

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(views);
        var expected = mapper.readTree(getClass()
                .getResource("/META-INF/VAADIN/available-views-admin.json"));

        MatcherAssert.assertThat(actual, Matchers.is(expected));
    }

    @Test
    public void should_collectServerViews() {
        final Map<String, AvailableViewInfo> views;

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            views = requestListener.collectServerViews();
        }
        MatcherAssert.assertThat(views, Matchers.aMapWithSize(5));
        MatcherAssert.assertThat(views.get("/bar").title(),
                Matchers.is("Component"));
        MatcherAssert.assertThat(views.get("/foo").title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(views.get("/bar").route(),
                Matchers.is("/bar"));
        MatcherAssert.assertThat(
                views.get("/wildcard/:___wildcard*").routeParameters(),
                Matchers.is(Map.of(":___wildcard*", RouteParamType.WILDCARD)));
        MatcherAssert.assertThat(
                views.get("//:___userId/edit").routeParameters(),
                Matchers.is(Map.of(":___userId", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
                views.get("/comments/:___commentId?").routeParameters(),
                Matchers.is(Map.of(":___commentId?", RouteParamType.OPTIONAL)));

    }

    @Test
    public void when_productionMode_should_collectClientViews()
            throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(true);
        when(vaadinRequest.getUserPrincipal())
                .thenReturn(Mockito.mock(UserPrincipal.class));
        when(vaadinRequest.isUserInRole(Mockito.anyString())).thenReturn(true);

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
            var views = requestListener.collectClientViews(vaadinRequest);
            MatcherAssert.assertThat(views, Matchers.aMapWithSize(3));
        }
    }

    @Test
    public void when_developmentMode_should_collectClientViews()
            throws IOException {
        mockDevelopmentMode();
        when(vaadinRequest.getUserPrincipal())
                .thenReturn(Mockito.mock(UserPrincipal.class));
        when(vaadinRequest.isUserInRole(Mockito.anyString())).thenReturn(true);

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);
            var views = requestListener.collectClientViews(vaadinRequest);
            MatcherAssert.assertThat(views, Matchers.aMapWithSize(3));
        }
    }

    @Test
    public void when_exposeServerRoutesToClient_false_serverSideRoutesAreNotInResponse()
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
            Mockito.when(vaadinRequest.isUserInRole(Mockito.anyString()))
                    .thenReturn(true);
            var requestListener = new RouteUnifyingIndexHtmlRequestListener(
                    clientRouteRegistry, deploymentConfiguration, routeUtil,
                    null, null, false);

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

        final var mapper = new ObjectMapper();

        var actual = mapper.readTree(views);
        var expected = mapper.readTree(getClass()
                .getResource("/META-INF/VAADIN/only-client-views.json"));

        MatcherAssert.assertThat(actual, Matchers.is(expected));
    }

    private void mockDevelopmentMode() throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(false);
        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(deploymentConfiguration.getFrontendFolder())
                .thenReturn(frontendGeneratedDir.getParentFile());

        File clientFiles = new File(frontendGeneratedDir,
                FILE_ROUTES_JSON_NAME);
        Files.writeString(clientFiles.toPath(), clientRoutes);
    }

    @PageTitle("RouteTarget")
    private static class RouteTarget extends Component {
    }

    String clientRoutes = """
            [
              {
                "route": "/home",
                "params": {},
                "title": "Home",
                "children": []
              },

              {
                "route": "/profile",
                "params": {},
                "title": "Profile",
                "loginRequired": true,
                "rolesAllowed": [ "ROLE_USER", "ROLE_ADMIN"]
              },
              {
                "route": "/user/:userId",
                "loginRequired": true,
                "rolesAllowed": ["ROLE_ADMIN"],
                "params": { ":userId": "req"},
                "title": "User Profile"
              }
            ]
            """;
}
