package com.vaadin.flow.server.connect;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.connect.auth.VaadinConnectAccessChecker;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * A helper class to build a mocked VaadinConnectController.
 */
public class VaadinConnectControllerMockBuilder {
    private ApplicationContext applicationContext;
    private ObjectMapper objectMapper;
    private EndpointNameChecker endpointNameChecker = mock(EndpointNameChecker.class);
    private ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(ExplicitNullableTypeChecker.class);

    public VaadinConnectControllerMockBuilder withApplicationContext(
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    public VaadinConnectControllerMockBuilder withObjectMapper(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public VaadinConnectControllerMockBuilder withEndpointNameChecker(
            EndpointNameChecker endpointNameChecker) {
        this.endpointNameChecker = endpointNameChecker;
        return this;
    }

    public VaadinConnectControllerMockBuilder withExplicitNullableTypeChecker(
            ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        return this;
    }

    public VaadinConnectController build() {
        VaadinConnectController controller = Mockito.spy(
            new VaadinConnectController(objectMapper, endpointNameChecker,
                explicitNullableTypeChecker, applicationContext));
        Mockito.doReturn(mock(VaadinConnectAccessChecker.class))
                .when(controller).getAccessChecker(Mockito.any());
        return controller;
    }
}
