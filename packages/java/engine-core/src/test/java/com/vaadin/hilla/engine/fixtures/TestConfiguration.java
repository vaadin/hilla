package com.vaadin.hilla.engine.fixtures;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TestEndpoint.class, TestBrowserCallable.class})
public class TestConfiguration {
}
