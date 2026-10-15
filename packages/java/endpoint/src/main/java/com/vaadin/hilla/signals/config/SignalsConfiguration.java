/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.signals.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.ConditionalOnFeatureFlag;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.signals.Signal;
import com.vaadin.hilla.signals.core.registry.SecureSignalsRegistry;
import com.vaadin.hilla.signals.handler.SignalsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans configuration for signals.
 */
@Configuration
public class SignalsConfiguration {

    private SecureSignalsRegistry signalsRegistry;
    private SignalsHandler signalsHandler;
    private final EndpointInvoker endpointInvoker;

    public SignalsConfiguration(EndpointInvoker endpointInvoker,
            @Qualifier("hillaEndpointObjectMapper") ObjectMapper hillaEndpointObjectMapper) {
        this.endpointInvoker = endpointInvoker;
        Signal.setMapper(hillaEndpointObjectMapper);
    }

    /**
     * Initializes the SignalsRegistry bean when the fullstackSignals feature
     * flag is enabled.
     *
     * @return SignalsRegistry bean instance
     */
    @ConditionalOnFeatureFlag("fullstackSignals")
    @Bean
    public SecureSignalsRegistry signalsRegistry() {
        if (signalsRegistry == null) {
            signalsRegistry = new SecureSignalsRegistry(endpointInvoker);
        }
        return signalsRegistry;
    }

    /**
     * Initializes the SignalsHandler endpoint when the fullstackSignals feature
     * flag is enabled.
     *
     * @return SignalsHandler endpoint instance
     */
    @Bean
    public SignalsHandler signalsHandler(
            @Autowired(required = false) SecureSignalsRegistry signalsRegistry) {
        if (signalsHandler == null) {
            signalsHandler = new SignalsHandler(signalsRegistry);
        }
        return signalsHandler;
    }
}
