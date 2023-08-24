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

class EndpointHotSwapListener implements HotSwapListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointHotSwapListener.class);

    private final EndpointController endpointController;

    private final EndpointHotSwapService endpointHotSwapService;

    public EndpointHotSwapListener(EndpointController endpointController,
            EndpointHotSwapService endpointHotSwapService) {
        this.endpointController = endpointController;
        this.endpointHotSwapService = endpointHotSwapService;
    }

    @PostConstruct
    private void registerForHotSwapChanges() {
        endpointHotSwapService.addHotSwapListener(this);
    }

    @Override
    public void endpointChanged(EndpointChangedEvent event) {
        EngineConfiguration engineConfiguration;
        try {
            engineConfiguration = EngineConfiguration
                    .loadDirectory(event.buildDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ParserProcessor parser = new ParserProcessor(engineConfiguration,
                this.getClass().getClassLoader());
        parser.process();
        GeneratorProcessor generator = new GeneratorProcessor(
                engineConfiguration, "node");
        generator.process();

        this.endpointController.registerEndpoints();
        reload(event.browserLiveReload());
    }

    private void reload(BrowserLiveReload browserLiveReload) {

        Optional.ofNullable(browserLiveReload).ifPresent(liveReload -> {
            LOGGER.debug("Reloading the browser after endpoint(s) changes...");
            liveReload.reload();
        });
    }

}
