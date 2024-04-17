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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
        createMockedDevModeFileRouteJson();

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration,
                LocalDateTime.now());
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));

        clientRouteRegistry.clearRoutes();
        MatcherAssert.assertThat(clientRouteRegistry.getAllRoutes(),
                Matchers.anEmptyMap());
    }

    @Test
    public void when_developmentMode_and_noFileRouteJsonFile_then_noRoutesAreRegistered()
            throws IOException {

        mockDevelopmentMode();

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration,
                LocalDateTime.now());
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.anEmptyMap());
    }

    @Test
    public void when_developmentMode_and_emptyFileRouteJsonFile_then_noRoutesAreRegistered()
            throws IOException {

        mockDevelopmentMode();

        projectRoot.newFile("frontend/generated/file-routes.json");

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration,
                LocalDateTime.now());
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.anEmptyMap());
    }

    @Test
    public void when_productionMode_then_loadClientViewsFromResources() {

        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(true);

        clientRouteRegistry.registerClientRoutes(deploymentConfiguration,
                LocalDateTime.now());
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
        createMockedDevModeFileRouteJson();

        clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                deploymentConfiguration);
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

    @Test
    public void when_developmentMode_then_loadLatestDevModeFileRoutesJsonIfNeeded_loads_only_when_fileRoutesJson_changes()
            throws IOException {

        mockDevelopmentMode();
        createMockedDevModeFileRouteJson();

        clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                deploymentConfiguration);
        var allRoutes = clientRouteRegistry.getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));
        clientRouteRegistry.clearRoutes();

        clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                deploymentConfiguration);
        allRoutes = clientRouteRegistry.getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(0));

        createMockedDevModeFileRouteJson();

        clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                deploymentConfiguration);
        allRoutes = clientRouteRegistry.getAllRoutes();
        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));
    }

    private void mockDevelopmentMode() throws IOException {
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(false);
        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(deploymentConfiguration.getFrontendFolder())
                .thenReturn(frontendGeneratedDir.getParentFile());
    }

    private void createMockedDevModeFileRouteJson() throws IOException {
        var fileRoutesJsonProdAsResource = getClass()
                .getResource(ClientRouteRegistry.FILE_ROUTES_JSON_PROD_PATH);
        assert fileRoutesJsonProdAsResource != null;
        String hierarchicalRoutesAsString = IOUtils.toString(
                fileRoutesJsonProdAsResource.openStream(),
                StandardCharsets.UTF_8);
        String addedDevToRootRoute = "[{ \"route\": \"dev\", \"children\": "
                + hierarchicalRoutesAsString + " }]";
        final String fileRoutesJsonPath = "frontend/generated/"
                + ClientRouteRegistry.FILE_ROUTES_JSON_NAME;
        File fileRoutesJsonFile = projectRoot.getRoot().toPath()
                .resolve(fileRoutesJsonPath).toFile();
        if (!fileRoutesJsonFile.exists()) {
            projectRoot.newFile(fileRoutesJsonPath);
        }
        try (PrintWriter writer = new PrintWriter(fileRoutesJsonFile)) {
            writer.println(addedDevToRootRoute);
        }
    }
}
