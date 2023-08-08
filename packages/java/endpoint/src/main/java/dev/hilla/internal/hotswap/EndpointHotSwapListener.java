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

import dev.hilla.EndpointController;
import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserProcessor;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.hilla.internal.hotswap.HotSwapEvent.Type.OPEN_API_JSON;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.utils.JsonPrinter;

class EndpointHotSwapListener implements HotSwapListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointHotSwapListener.class);

    private final EndpointController endpointController;

    private final HotSwapWatchService hotSwapWatchService;

    public EndpointHotSwapListener(EndpointController endpointController,
            HotSwapWatchService hotSwapWatchService) {
        this.endpointController = endpointController;
        this.hotSwapWatchService = hotSwapWatchService;
    }

    @PostConstruct
    private void registerForHotSwapChanges() {
        hotSwapWatchService.addHotSwapListener(this);
        pollEndpoints();
    }

    private static class ExecutorStatus {
        EngineConfiguration engineConfiguration;
        String openAPI;
    }

    private void pollEndpoints() {
        var status = new ExecutorStatus();
        ScheduledExecutorService executor = Executors
                .newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (status.engineConfiguration == null) {
                Path buildDir = Path.of(
                        "/home/luciano/vaadin/temp-projects/hotswap/target");
                try {
                    status.engineConfiguration = EngineConfiguration
                            .loadDirectory(buildDir);
                    status.engineConfiguration.getParser()
                            .setPackages(List.of());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    ParserProcessor parserProcessor = new ParserProcessor(
                            status.engineConfiguration,
                            this.getClass().getClassLoader());
                    var newOpenAPI = parserProcessor.createOpenAPI();
                    if (status.openAPI == null) {
                        status.openAPI = newOpenAPI;
                    } else if (!status.openAPI.equals(newOpenAPI)) {
                        status.openAPI = newOpenAPI;
                        System.out.println("OpenAPI definition changed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);

        // Add a shutdown hook to stop the executor when the application is
        // shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
        }));
    }

    @Override
    public void onHotSwapEvent(HotSwapEvent event) {
        // if (OPEN_API_JSON == event.type()) {
        // this.endpointController.registerEndpoints();
        // reload(event);
        // } else {
        // Path buildDir = event.classesDir().getParent();
        // EngineConfiguration engineConfiguration;
        // try {
        // engineConfiguration = EngineConfiguration
        // .loadDirectory(buildDir);
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
        // ParserProcessor parser = new ParserProcessor(engineConfiguration,
        // this.getClass().getClassLoader());
        // parser.process();
        // GeneratorProcessor generator = new GeneratorProcessor(
        // engineConfiguration, "node");
        // generator.process();
        // }
    }

    private void reload(HotSwapEvent event) {

        Optional.ofNullable(event.browserLiveReload())
                .ifPresent(browserLiveReload -> {
                    LOGGER.debug(
                            "Reloading the browser after endpoint(s) changes...");
                    browserLiveReload.reload();
                });
    }

}
