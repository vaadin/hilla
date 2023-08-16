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

package dev.hilla.internal.hotswap;

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;

class HotSwapServiceInitializer implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HotSwapServiceInitializer.class);

    @Value("${hilla.endpoint.hot-reload.enabled:true}")
    private boolean endpointHotReloadEnabled;

    private final EndpointHotSwapService endpointHotSwapService;

    public HotSwapServiceInitializer(
            EndpointHotSwapService endpointHotSwapService) {
        this.endpointHotSwapService = endpointHotSwapService;
    }

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        VaadinService vaadinService = serviceInitEvent.getSource();
        BrowserLiveReloadAccessor.getLiveReloadFromService(vaadinService)
                .ifPresent(browserLiveReload -> {
                    if (BrowserLiveReload.Backend.SPRING_BOOT_DEVTOOLS != browserLiveReload
                            .getBackend()
                            && isDevModeLiveReloadEnabled(vaadinService)) {
                        if (isEndpointHotReloadEnabled()) {
                            endpointHotSwapService.monitorChanges(
                                    getClassesDir(vaadinService),
                                    browserLiveReload);
                            info("Hilla Endpoint Hot-Reload service is enabled. "
                                    + "You can disable it by defining the hilla.endpoint.hot-reload.enabled=false property in application.properties file.");
                        } else {
                            info("Hilla Endpoint Hot-Reload service is disabled. "
                                    + "You can enable it by removing the hilla.endpoint.hot-reload.enabled=true property from your application.properties file, "
                                    + "or by setting its value to true.");
                        }
                    }
                });
    }

    private boolean isDevModeLiveReloadEnabled(VaadinService vaadinService) {
        return vaadinService.getDeploymentConfiguration()
                .isDevModeLiveReloadEnabled();
    }

    private Path getClassesDir(VaadinService vaadinService) {
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
