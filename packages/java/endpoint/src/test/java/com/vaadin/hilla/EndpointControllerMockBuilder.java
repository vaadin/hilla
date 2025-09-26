package com.vaadin.hilla;

import static org.mockito.Mockito.mock;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.vaadin.hilla.endpointransfermapper.EndpointTransferMapper;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.vaadin.hilla.auth.CsrfChecker;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;

import jakarta.servlet.ServletContext;

/**
 * A helper class to build a mocked EndpointController.
 */
public class EndpointControllerMockBuilder {
    private static final EndpointTransferMapper ENDPOINT_TRANSFER_MAPPER = new EndpointTransferMapper();
    private ApplicationContext applicationContext;
    private EndpointNameChecker endpointNameChecker = mock(
            EndpointNameChecker.class);
    private ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
            ExplicitNullableTypeChecker.class);
    private JacksonObjectMapperFactory factory;

    public EndpointController build() {
        EndpointRegistry registry = new EndpointRegistry(endpointNameChecker);
        CsrfChecker csrfChecker = Mockito.mock(CsrfChecker.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(csrfChecker.validateCsrfTokenInRequest(Mockito.any()))
                .thenReturn(true);
        ObjectMapper endpointObjectMapper = createEndpointObjectMapper(
                applicationContext, factory);
        EndpointInvoker invoker = Mockito.spy(
                new EndpointInvoker(applicationContext, endpointObjectMapper,
                        explicitNullableTypeChecker, servletContext, registry));
        EndpointController controller = Mockito
                .spy(new EndpointController(applicationContext, registry,
                        invoker, csrfChecker, endpointObjectMapper));
        Mockito.doReturn(mock(EndpointAccessChecker.class)).when(invoker)
                .getAccessChecker();
        return controller;
    }

    public static ObjectMapper createEndpointObjectMapper(
            ApplicationContext applicationContext,
            JacksonObjectMapperFactory factory) {
        ObjectMapper endpointObjectMapper = factory != null ? factory.build()
                : createDefaultEndpointMapper(applicationContext);
        if (endpointObjectMapper != null) {
            // In Jackson 3, we need to rebuild to add modules
            // rebuild() preserves the configuration from the original mapper
            endpointObjectMapper = endpointObjectMapper.rebuild()
                    .addModule(ENDPOINT_TRANSFER_MAPPER.getJacksonModule())
                    .build();
        }
        return endpointObjectMapper;
    }

    private static ObjectMapper createDefaultEndpointMapper(
            ApplicationContext applicationContext) {
        // JacksonObjectMapperFactory.Json already configures date/time properly
        return new JacksonObjectMapperFactory.Json().build();
    }

    public EndpointControllerMockBuilder withApplicationContext(
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    public EndpointControllerMockBuilder withEndpointNameChecker(
            EndpointNameChecker endpointNameChecker) {
        this.endpointNameChecker = endpointNameChecker;
        return this;
    }

    public EndpointControllerMockBuilder withExplicitNullableTypeChecker(
            ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        return this;
    }

    public EndpointControllerMockBuilder withObjectMapperFactory(
            JacksonObjectMapperFactory factory) {
        this.factory = factory;
        return this;
    }
}
