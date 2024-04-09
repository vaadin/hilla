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

package com.vaadin.hilla.internal.hotswap;

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.shared.Registration;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class PollChangedEndpointsHotSwapService implements EndpointHotSwapService {

    private static class ExecutorStatus {

        EngineConfiguration engineConfiguration;

        String openAPI;
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PollChangedEndpointsHotSwapService.class);

    private final Collection<HotSwapListener> hotSwapListeners = new ArrayList<>();

    private final int endpointHotReloadPollInterval;

    private Path buildDir;

    private BrowserLiveReload browserLiveReload;

    /**
     * @param endpointHotReloadPollInterval
     *            The interval for polling the changes in seconds.
     */
    public PollChangedEndpointsHotSwapService(
            int endpointHotReloadPollInterval) {
        this.endpointHotReloadPollInterval = endpointHotReloadPollInterval;
    }

    @Override
    public void monitorChanges(Path buildDir,
            BrowserLiveReload browserLiveReload) {
        this.browserLiveReload = browserLiveReload;
        this.buildDir = buildDir;

        // start polling the endpoints
        pollEndpoints();
    }

    private void pollEndpoints() {
        debug("Initializing the polling thread for detecting endpoint(s) changes...");
        var status = initializeExecutorStatus();
        ScheduledExecutorService executor = Executors
                .newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                ParserProcessor parserProcessor = new ParserProcessor(
                        status.engineConfiguration,
                        this.getClass().getClassLoader(), false);
                var newOpenAPI = parserProcessor.createOpenAPI();
                if (status.openAPI == null) {
                    status.openAPI = newOpenAPI;
                } else if (!status.openAPI.equals(newOpenAPI)) {
                    status.openAPI = newOpenAPI;
                    fireEndpointChangedEvent();
                }
            } catch (Throwable e) {
                error("Error while polling for endpoint(s) changes: %s",
                        e.getMessage());
                e.printStackTrace();
            }

        }, 0, endpointHotReloadPollInterval, TimeUnit.SECONDS);
        debug("for detecting endpoint(s) changes initialized.");

        // Add a shutdown hook to stop the executor when the application is
        // shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    private void fireEndpointChangedEvent() {
        debug("Endpoint change(s) detected...");
        var event = new HotSwapListener.EndpointChangedEvent(buildDir,
                browserLiveReload);
        hotSwapListeners.forEach(listener -> listener.endpointChanged(event));
    }

    private ExecutorStatus initializeExecutorStatus() {
        ExecutorStatus status = new ExecutorStatus();
        try {
            status.engineConfiguration = EngineConfiguration
                    .loadDirectory(buildDir);
            status.engineConfiguration.getParser().setPackages(List.of());
            return status;
        } catch (IOException e) {
            error("Could not load the engine configuration from %s", buildDir);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Registration addHotSwapListener(HotSwapListener listener) {
        return Registration.addAndRemove(this.hotSwapListeners, listener);
    }

    private void debug(String message) {
        LOGGER.debug("### Hilla Endpoint changes Poll service > " + message);
    }

    private void error(String format, Object... params) {
        error(String.format(format, params));
    }

    private void error(String message) {
        LOGGER.error("### Hilla Endpoint changes Poll service > " + message);
    }
}
