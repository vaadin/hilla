/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.internal.hotswap;

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

class HotSwapServiceInitializer implements VaadinServiceInitListener {

    private final Logger LOGGER;

    boolean endpointHotReloadEnabled;

    private final EndpointHotSwapService endpointHotSwapService;

    public HotSwapServiceInitializer(
            EndpointHotSwapService endpointHotSwapService,
            boolean endpointHotReloadEnabled) {
        this.endpointHotSwapService = endpointHotSwapService;
        this.endpointHotReloadEnabled = endpointHotReloadEnabled;
        LOGGER = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        VaadinService vaadinService = serviceInitEvent.getSource();
        BrowserLiveReloadAccessor.getLiveReloadFromService(vaadinService)
                .ifPresent(browserLiveReload -> {
                    if (browserLiveReload.getBackend() != null
                            && BrowserLiveReload.Backend.SPRING_BOOT_DEVTOOLS != browserLiveReload
                                    .getBackend()
                            && isDevModeLiveReloadEnabled(vaadinService)) {
                        if (isEndpointHotReloadEnabled()) {
                            endpointHotSwapService.monitorChanges(
                                    getBuildDir(vaadinService),
                                    browserLiveReload);
                            info("Hilla Endpoint Hot-Reload service is enabled. "
                                    + "You can disable it by defining the hilla.endpoint.hot-reload.enabled=false property in application.properties file.");
                            info("The default polling interval for Hilla Endpoint Hot-Reload is 5 seconds. "
                                    + "You can change the interval by defining a positive value for the hilla.endpoint.hot-reload.pollInterval property in application.properties file.");
                        } else {
                            info("Hilla Endpoint Hot-Reload service is disabled. "
                                    + "You can enable it by adding the hilla.endpoint.hot-reload.enabled=true property to your application.properties file, "
                                    + "or by setting its value to true.");
                        }
                    }
                });
    }

    private boolean isDevModeLiveReloadEnabled(VaadinService vaadinService) {
        return vaadinService.getDeploymentConfiguration()
                .isDevModeLiveReloadEnabled();
    }

    private Path getBuildDir(VaadinService vaadinService) {
        var deploymentConfig = vaadinService.getDeploymentConfiguration();
        var projectFolder = deploymentConfig.getProjectFolder().toPath();
        return projectFolder.resolve(deploymentConfig.getBuildFolder());
    }

    private boolean isEndpointHotReloadEnabled() {
        return endpointHotReloadEnabled;
    }

    private void info(String message) {
        LOGGER.info(message);
    }
}
