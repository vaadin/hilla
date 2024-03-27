package com.vaadin.hilla.route;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.hilla.route.records.ClientViewConfig;
import com.vaadin.hilla.route.records.RouteParamType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientRouteRegistryTest {

    private final ClientRouteRegistry clientRouteRegistry = new ClientRouteRegistry();

    private final DeploymentConfiguration deploymentConfiguration = Mockito
            .mock(DeploymentConfiguration.class);

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    @Test
    public void when_clearRoutes_isCalled_then_allRoutesAreCleared()
            throws IOException {
        mockDevelopmentMode();
        createMockedDevModeViewsJson();

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));

        clientRouteRegistry.clearRoutes();
        MatcherAssert.assertThat(clientRouteRegistry.getAllRoutes(),
                Matchers.anEmptyMap());
    }

    @Test
    public void when_developmentMode_and_noViewsJsonFile_then_noRoutesAreRegistered()
            throws IOException {

        mockDevelopmentMode();

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.anEmptyMap());
    }

    @Test
    public void when_developmentMode_and_emptyViewsJsonFile_then_noRoutesAreRegistered()
            throws IOException {

        mockDevelopmentMode();

        projectRoot.newFile("frontend/generated/views.json");

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.anEmptyMap());
    }

    @Test
    public void when_productionMode_then_loadClientViewsFromResources() {

        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(true);

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/about").getTitle(),
                Matchers.is("About"));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/profile/friends/list")
                        .getOther().get("unknown"),
                Matchers.notNullValue());
        MatcherAssert.assertThat(
                clientRouteRegistry
                        .getRouteByPath("/profile/friends/:user?/edit")
                        .getRouteParameters(),
                Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/profile/friends/:user")
                        .getRouteParameters(),
                Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/profile/messages/*")
                        .getRouteParameters(),
                Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));
    }

    @Test
    public void when_developmentMode_then_loadClientViewsFromFrontendGenerated()
            throws IOException {

        mockDevelopmentMode();
        createMockedDevModeViewsJson();

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/dev/about").getTitle(),
                Matchers.is("About"));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/dev/profile/friends/list")
                        .getOther().get("unknown"),
                Matchers.notNullValue());
        MatcherAssert
                .assertThat(
                        clientRouteRegistry
                                .getRouteByPath(
                                        "/dev/profile/friends/:user?/edit")
                                .getRouteParameters(),
                        Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/dev/profile/friends/:user")
                        .getRouteParameters(),
                Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
                clientRouteRegistry.getRouteByPath("/dev/profile/messages/*")
                        .getRouteParameters(),
                Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));
    }

    private void mockDevelopmentMode() throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(false);
        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(deploymentConfiguration.getFrontendFolder())
                .thenReturn(frontendGeneratedDir.getParentFile());
    }

    private void createMockedDevModeViewsJson() throws IOException {
        var viewsJsonProdAsResource = getClass()
                .getResource("/META-INF/VAADIN/views.json");
        assert viewsJsonProdAsResource != null;
        String hierarchicalRoutesAsString = IOUtils.toString(
                viewsJsonProdAsResource.openStream(), StandardCharsets.UTF_8);
        String addedDevToRootRoute = hierarchicalRoutesAsString
                .replaceFirst("\"route\": \"\",", "\"route\": \"dev\",");
        var viewsJsonFile = projectRoot
                .newFile("frontend/generated/views.json");
        try (PrintWriter writer = new PrintWriter(viewsJsonFile)) {
            writer.println(addedDevToRootRoute);
        }
    }
}
