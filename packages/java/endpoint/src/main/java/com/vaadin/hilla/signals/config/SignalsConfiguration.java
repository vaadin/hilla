package com.vaadin.hilla.signals.config;

import com.vaadin.hilla.signals.core.SignalsHandler;
import com.vaadin.hilla.signals.core.SignalsRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SignalsConfiguration {

    private final SignalsRegistry signalsRegistry;
    private final SignalsHandler signalsHandler;

    public SignalsConfiguration(SignalsRegistry signalsRegistry,
            SignalsHandler signalsHandler) {
        this.signalsRegistry = signalsRegistry;
        this.signalsHandler = signalsHandler;
    }

    @Bean
    public SignalsRegistry signalsRegistry() {
        return signalsRegistry;
    }

    @Bean
    public SignalsHandler signalsHandler() {
        return signalsHandler;
    }
}
