package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.EndpointController;
import org.springframework.stereotype.Component;

@Component
public class EndpointRegistryInitializer implements VaadinServiceInitListener {

    private final EndpointController endpointController;

    public EndpointRegistryInitializer(EndpointController endpointController) {
        this.endpointController = endpointController;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        endpointController.registerEndpoints();
    }
}
