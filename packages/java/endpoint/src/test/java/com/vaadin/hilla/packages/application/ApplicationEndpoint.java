package com.vaadin.hilla.packages.application;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class ApplicationEndpoint {
    private final ApplicationComponent applicationComponent;

    public ApplicationEndpoint(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public String getMessage() {
        return applicationComponent.getMessage();
    }
}
