package com.vaadin.hilla.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.vaadin.flow.function.DeploymentConfiguration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteExtractionIndexHtmlRequestListener;
import com.vaadin.hilla.route.records.ClientViewConfig;
import com.vaadin.hilla.route.records.RouteParamType;

public class RouteUnifyingServiceInitListenerTest {

    private RouteUnifyingServiceInitListener routeUnifyingServiceInitListener;
    private ServiceInitEvent event;
    private ClientRouteRegistry clientRouteRegistry;
    private DeploymentConfiguration mockDeploymentConfiguration;

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        clientRouteRegistry = new ClientRouteRegistry();
        routeUnifyingServiceInitListener = new RouteUnifyingServiceInitListener(
                Mockito.mock(RouteExtractionIndexHtmlRequestListener.class),
                clientRouteRegistry);
        VaadinService mockVaadinService = Mockito.mock(VaadinService.class);
        mockDeploymentConfiguration = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(mockVaadinService.getDeploymentConfiguration())
                .thenReturn(mockDeploymentConfiguration);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
            .thenReturn(true);
        event = new ServiceInitEvent(mockVaadinService);
    }

    @Test
    public void should_addRouteIndexHtmlRequestListener() {
        Assert.assertFalse("Unexpected RouteIndexHtmlRequestListener added",
                eventHasAddedRouteIndexHtmlRequestListener(event));
        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "Expected event to have RouteIndexHtmlRequestListener added",
                eventHasAddedRouteIndexHtmlRequestListener(event));
    }

    @Test
    public void should_extractClientViews_fromResources_inProdMode() {
        routeUnifyingServiceInitListener.registerClientRoutes(
                event.getSource().getDeploymentConfiguration());
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(8));
        MatcherAssert.assertThat(allRoutes.get("/about").title(),
                Matchers.is("About"));
        MatcherAssert.assertThat(
                allRoutes.get("/profile/friends/list").other().get("unknown"),
                Matchers.notNullValue());
        MatcherAssert.assertThat(
                allRoutes.get("/profile/friends/:user?/edit").routeParameters(),
                Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(
                allRoutes.get("/profile/friends/:user").routeParameters(),
                Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
                allRoutes.get("/profile/messages/*").routeParameters(),
                Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));

    }

    @Test
    public void should_extractClientViews_fromFrontendGenerated_inDevMode() throws IOException {
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
            .thenReturn(false);
        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(mockDeploymentConfiguration.getFrontendFolder())
            .thenReturn(frontendGeneratedDir.getParentFile());

        createDevModeViewsJson();

        routeUnifyingServiceInitListener.registerClientRoutes(
            event.getSource().getDeploymentConfiguration());
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
            .getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(8));
        MatcherAssert.assertThat(allRoutes.get("/dev/about").title(),
            Matchers.is("About"));
        MatcherAssert.assertThat(
            allRoutes.get("/dev/profile/friends/list").other().get("unknown"),
            Matchers.notNullValue());
        MatcherAssert.assertThat(
            allRoutes.get("/dev/profile/friends/:user?/edit").routeParameters(),
            Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(
            allRoutes.get("/dev/profile/friends/:user").routeParameters(),
            Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
            allRoutes.get("/dev/profile/messages/*").routeParameters(),
            Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));
    }

    private void createDevModeViewsJson() throws IOException {
        var viewsJsonFile = projectRoot.newFile("frontend/generated/views.json");
        try (PrintWriter writer = new PrintWriter(viewsJsonFile)){
            writer.println("""
                {
                  "/dev/about": { "title": "About" },
                  "/dev/profile/": { "title": "Profile" },
                  "/dev/profile/account/security/password": { "title": "Password" },
                  "/dev/profile/account/security/two-factor-auth": { "title": "Two Factor Auth" },
                  "/dev/profile/friends/list": { "title": "List", "unknown": {"anotherProp" :  "prop"} },
                  "/dev/profile/friends/:user?/edit": { "title": "Friend Edit", "params": { ":user?":  "opt"} },
                  "/dev/profile/friends/:user": { "title": "Friend Profile", "params" : { ":user":  "req"} },
                  "/dev/profile/messages/*": { "title": "Messages", "params" : { "wildcard":  "*"} }
                }
                """.stripIndent());
        }
    }

    private boolean eventHasAddedRouteIndexHtmlRequestListener(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteExtractionIndexHtmlRequestListener);
    }
}
