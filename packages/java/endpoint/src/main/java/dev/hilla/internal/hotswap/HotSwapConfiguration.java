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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotSwapConfiguration {

    /**
     * The interval for polling the changes in seconds.
     */
    @Value("${hilla.endpoint.hot-reload.pollInterval:5}")
    private int endpointHotReloadPollInterval;

    @Bean
    EndpointHotSwapService hotSwapWatchService() {
        return new PollChangedEndpointsHotSwapService(
                endpointHotReloadPollInterval);
    }

    @Bean
    HotSwapServiceInitializer hotSwapServiceInitializer(
            @Autowired EndpointHotSwapService endpointHotSwapService) {
        return new HotSwapServiceInitializer(endpointHotSwapService);
    }

    @Bean
    EndpointHotSwapListener endpointHotSwapListener(
            @Autowired EndpointHotSwapService endpointHotSwapService,
            @Autowired EndpointController endpointController) {
        return new EndpointHotSwapListener(endpointController,
                endpointHotSwapService);
    }
}
