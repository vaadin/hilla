package com.vaadin.fusion;

import static org.mockito.Mockito.mock;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.di.Lookup;
import com.vaadin.fusion.auth.CsrfChecker;
import com.vaadin.fusion.auth.FusionAccessChecker;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * A helper class to build a mocked FusionController.
 */
public class FusionControllerMockBuilder {
    private ApplicationContext applicationContext;
    private ObjectMapper objectMapper;
    private EndpointNameChecker endpointNameChecker = mock(
            EndpointNameChecker.class);
    private ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
            ExplicitNullableTypeChecker.class);

    public FusionControllerMockBuilder withApplicationContext(
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    public FusionControllerMockBuilder withObjectMapper(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public FusionControllerMockBuilder withEndpointNameChecker(
            EndpointNameChecker endpointNameChecker) {
        this.endpointNameChecker = endpointNameChecker;
        return this;
    }

    public FusionControllerMockBuilder withExplicitNullableTypeChecker(
            ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        return this;
    }

    public FusionController build() {
        EndpointRegistry registry = new EndpointRegistry(endpointNameChecker);
        CsrfChecker csrfChecker = Mockito.mock(CsrfChecker.class);
        Mockito.when(csrfChecker.validateCsrfTokenInRequest(Mockito.any()))
                .thenReturn(true);
        FusionController controller = Mockito.spy(
                new FusionController(objectMapper, explicitNullableTypeChecker,
                        applicationContext, registry, csrfChecker));
        Mockito.doReturn(mock(FusionAccessChecker.class)).when(controller)
                .getAccessChecker(Mockito.any());
        return controller;
    }
}
