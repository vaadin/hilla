/*
 * Copyright 2000-2024 Vaadin Ltd.
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

package com.vaadin.hilla.signals.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.ConditionalOnFeatureFlag;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.signals.Signal;
import com.vaadin.hilla.signals.core.registry.SecureSignalsRegistry;
import com.vaadin.hilla.signals.handler.SignalsHandler;
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
            ObjectMapper hillaEndpointObjectMapper) {
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
    @ConditionalOnFeatureFlag("fullstackSignals")
    @Bean
    public SignalsHandler signalsHandler() {
        if (signalsHandler == null) {
            signalsHandler = new SignalsHandler(signalsRegistry());
        }
        return signalsHandler;
    }
}
