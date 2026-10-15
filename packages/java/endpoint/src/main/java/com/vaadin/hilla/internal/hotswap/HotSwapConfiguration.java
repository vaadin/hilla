/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.internal.hotswap;

import com.vaadin.hilla.Hotswapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotSwapConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hilla.endpoint.hot-reload")
    public HotSwapConfigurationProperties hotSwapConfigurationProperties() {
        return new HotSwapConfigurationProperties();
    }

    @Bean
    EndpointHotSwapService hotSwapWatchService(
            @Autowired HotSwapConfigurationProperties configurationProperties) {
        return new PollChangedEndpointsHotSwapService(
                configurationProperties.getPollInterval());
    }

    @Bean
    HotSwapServiceInitializer hotSwapServiceInitializer(
            @Autowired EndpointHotSwapService endpointHotSwapService,
            @Autowired HotSwapConfigurationProperties configurationProperties) {
        return new HotSwapServiceInitializer(endpointHotSwapService,
                configurationProperties.isEnabled() && !Hotswapper.isInUse());
    }

    @Bean
    EndpointHotSwapListener endpointHotSwapListener(
            @Autowired EndpointHotSwapService endpointHotSwapService) {
        return new EndpointHotSwapListener(endpointHotSwapService);
    }
}
