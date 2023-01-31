package dev.hilla;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import dev.hilla.auth.CsrfChecker;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.parser.jackson.JacksonObjectMapperFactory;

import jakarta.servlet.ServletContext;

/**
 * A helper class to build a mocked EndpointController.
 */
public class EndpointControllerMockBuilder {
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
        EndpointInvoker invoker = Mockito
                .spy(new EndpointInvoker(applicationContext, factory,
                        explicitNullableTypeChecker, servletContext, registry));
        EndpointController controller = Mockito.spy(new EndpointController(
                applicationContext, registry, invoker, csrfChecker));
        Mockito.doReturn(mock(EndpointAccessChecker.class)).when(invoker)
                .getAccessChecker();
        return controller;
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
