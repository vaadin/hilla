package com.vaadin.hilla.signals.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.ConditionalOnFeatureFlag;
import com.vaadin.hilla.signals.core.SignalsHandler;
import com.vaadin.hilla.signals.core.SignalsRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SignalsConfiguration {

    private SignalsRegistry signalsRegistry;
    private SignalsHandler signalsHandler;
    private final ObjectMapper objectMapper;

    public SignalsConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SignalsRegistry signalsRegistry() {
        if (signalsRegistry == null) {
            signalsRegistry = new SignalsRegistry();
        }
        return signalsRegistry;
    }

    @ConditionalOnFeatureFlag("fullstackSignals")
    @Bean
    public SignalsHandler signalsHandler() {
        if (signalsHandler == null) {
            signalsHandler = new SignalsHandler(signalsRegistry(),
                    objectMapper);
        }
        return signalsHandler;
    }
}
