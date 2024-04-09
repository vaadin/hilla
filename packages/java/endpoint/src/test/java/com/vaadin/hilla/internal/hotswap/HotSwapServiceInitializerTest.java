package com.vaadin.hilla.internal.hotswap;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class HotSwapServiceInitializerTest {

    private DeploymentConfiguration deploymentConfiguration;
    private VaadinService vaadinService;
    private ServiceInitEvent serviceInitEvent;
    private EndpointHotSwapService spyEndpointHotSwapService;
    private BrowserLiveReload mockedBrowserLiveReload;
    private HotSwapServiceInitializer hotSwapServiceInitializer;

    @Before
    public void init() throws IOException {
        deploymentConfiguration = Mockito
                .mock(DefaultDeploymentConfiguration.class);
        File projectFolder = Files.createTempDirectory("temp-project").toFile();
        String buildFolderName = "target";
        projectFolder.toPath().resolve(buildFolderName).toFile().mkdir();
        Mockito.when(deploymentConfiguration.getProjectFolder())
                .thenReturn(projectFolder);
        Mockito.when(deploymentConfiguration.getBuildFolder())
                .thenReturn(buildFolderName);

        vaadinService = Mockito.mock(VaadinService.class);
        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);

        serviceInitEvent = new ServiceInitEvent(vaadinService);

        spyEndpointHotSwapService = Mockito.spy(EndpointHotSwapService.class);

        mockedBrowserLiveReload = Mockito.mock(BrowserLiveReload.class);

        hotSwapServiceInitializer = new HotSwapServiceInitializer(
                spyEndpointHotSwapService, true);
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsDisabled_monitorChangesFromHotSwapServiceDoesNotGetCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsDisabledEndpointHotReloadIsDisabled_monitorChangesFromHotSwapServiceDoesNotGetCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);

            hotSwapServiceInitializer.endpointHotReloadEnabled = false;

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsEnabled_monitorChangesFromHotSwapServiceGetsCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.atLeastOnce())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsJRebelAndDevModeLiveReloadIsEnabled_monitorChangesFromHotSwapServiceGetsCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));

            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);

            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.atLeastOnce())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadIsNull_monitorChangesFromHotSwapServiceDoesNotGetCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.empty());

            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsNull_monitorChangesFromHotSwapServiceDoesNotGetCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));

            Mockito.when(mockedBrowserLiveReload.getBackend()).thenReturn(null);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsSpringBootDevTools_monitorChangesFromHotSwapServiceDoesNotGetCalled() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.empty());
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsEnabled_monitorChangesFromHotSwapServiceGetsCalledWithCorrectBuildFolder() {
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            var buildDir = deploymentConfiguration.getProjectFolder().toPath()
                    .resolve(deploymentConfiguration.getBuildFolder());
            Mockito.verify(spyEndpointHotSwapService, Mockito.atLeastOnce())
                    .monitorChanges(Mockito.eq(buildDir), Mockito.any());
        }
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsEnabled_loggerInfoIsCalledWithCorrectInfoWhenMonitorChangesFromHotSwapServiceGetsCalled() {
        Logger mockedLogger = Mockito.spy(Logger.class);
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class);
                var loggerFactoryMockedStatic = Mockito
                        .mockStatic(LoggerFactory.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            loggerFactoryMockedStatic
                    .when(() -> LoggerFactory
                            .getLogger(hotSwapServiceInitializer.getClass()))
                    .thenReturn(mockedLogger);
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            // for the sake of logger factory being mocked static,
            // hotSwapServiceInitializer should be created in the try block:
            hotSwapServiceInitializer = new HotSwapServiceInitializer(
                    spyEndpointHotSwapService, true);
            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(mockedLogger, Mockito.times(2)).info(Mockito.any());
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .info(Mockito.startsWith(
                            "Hilla Endpoint Hot-Reload service is enabled."));
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .info(Mockito.startsWith(
                            "The default polling interval for Hilla Endpoint Hot-Reload is 5 seconds."));
        }
    }

    @Test
    public void liveReloadBackendIsHotSwapAgentAndDevModeLiveReloadIsEnabledEndpointHotReloadIsDisabled_loggerInfoIsCalledWithCorrectInfo() {
        Logger mockedLogger = Mockito.spy(Logger.class);
        try (var browserLiveReloadAccessorMockedStatic = Mockito
                .mockStatic(BrowserLiveReloadAccessor.class);
                var loggerFactoryMockedStatic = Mockito
                        .mockStatic(LoggerFactory.class)) {
            browserLiveReloadAccessorMockedStatic
                    .when(() -> BrowserLiveReloadAccessor
                            .getLiveReloadFromService(vaadinService))
                    .thenReturn(Optional.of(mockedBrowserLiveReload));
            loggerFactoryMockedStatic
                    .when(() -> LoggerFactory
                            .getLogger(hotSwapServiceInitializer.getClass()))
                    .thenReturn(mockedLogger);
            Mockito.when(mockedBrowserLiveReload.getBackend())
                    .thenReturn(BrowserLiveReload.Backend.HOTSWAP_AGENT);
            Mockito.when(deploymentConfiguration.isDevModeLiveReloadEnabled())
                    .thenReturn(Boolean.TRUE);

            // for the sake of logger factory being mocked static,
            // hotSwapServiceInitializer should be created in the try block:
            hotSwapServiceInitializer = new HotSwapServiceInitializer(
                    spyEndpointHotSwapService, false);
            hotSwapServiceInitializer.serviceInit(serviceInitEvent);

            Mockito.verify(spyEndpointHotSwapService, Mockito.never())
                    .monitorChanges(Mockito.any(), Mockito.any());

            Mockito.verify(mockedLogger, Mockito.times(1)).info(Mockito.any());
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .info(Mockito.startsWith(
                            "Hilla Endpoint Hot-Reload service is disabled."));
        }
    }

}
