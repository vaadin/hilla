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
import java.nio.file.Path;
import java.util.Optional;

import static dev.hilla.internal.hotswap.HotSwapEvent.Type.OPEN_API_JSON;

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
    }

    @Override
    public void onHotSwapEvent(HotSwapEvent event) {
        if (OPEN_API_JSON == event.type()) {
            this.endpointController.registerEndpoints();
            reload(event);
        } else {
            Path buildDir = event.classesDir().getParent();
            EngineConfiguration engineConfiguration;
            try {
                engineConfiguration = EngineConfiguration
                        .loadDirectory(buildDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ParserProcessor parser = new ParserProcessor(engineConfiguration,
                    this.getClass().getClassLoader());
            parser.process();
            GeneratorProcessor generator = new GeneratorProcessor(
                    engineConfiguration, "node");
            generator.process();
        }
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
