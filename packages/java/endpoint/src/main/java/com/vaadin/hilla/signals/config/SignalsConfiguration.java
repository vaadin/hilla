package com.vaadin.hilla.signals.config;

import com.vaadin.hilla.ConditionalOnFeatureFlag;
import com.vaadin.hilla.EndpointInvoker;
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

    public SignalsConfiguration(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
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
