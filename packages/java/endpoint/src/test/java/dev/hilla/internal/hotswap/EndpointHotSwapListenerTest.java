package dev.hilla.internal.hotswap;

import com.vaadin.flow.di.Lookup;
import dev.hilla.*;
import dev.hilla.auth.CsrfChecker;
import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserProcessor;
import dev.hilla.parser.jackson.JacksonObjectMapperFactory;
import jakarta.servlet.ServletContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

public class EndpointHotSwapListenerTest {

    private EndpointHotSwapService spyEndpointHotSwapService;

    private EndpointController spyEndpointController;

    @Before
    public void init() {

        spyEndpointHotSwapService = Mockito.spy(EndpointHotSwapService.class);

        spyEndpointController = createVaadinController();
    }

    private <T> EndpointController createVaadinController() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(servletContext.getAttribute(Lookup.class.getName()))
                .thenReturn(lookup);

        JacksonObjectMapperFactory endpointMapperFactory = new JacksonObjectMapperFactory.Json();

        CsrfChecker csrfChecker = new CsrfChecker(servletContext);

        EndpointNameChecker endpointNameChecker = mock(
                EndpointNameChecker.class);

        ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
                ExplicitNullableTypeChecker.class);

        ApplicationContext mockApplicationContext = mock(
                ApplicationContext.class);
        EndpointRegistry registry = new EndpointRegistry(endpointNameChecker);

        EndpointInvoker invoker = Mockito
                .spy(new EndpointInvoker(mockApplicationContext,
                        endpointMapperFactory, explicitNullableTypeChecker,
                        mock(ServletContext.class), registry));

        EndpointController connectController = Mockito
                .spy(new EndpointController(mockApplicationContext, registry,
                        invoker, csrfChecker));
        connectController.registerEndpoints();
        return connectController;
    }

    @Test
    public void endpointChangedIsCalled_endpointControllerRegisterEndpointsIsCalled() {

        try (var engineConfigurationMockedStatic = Mockito
                .mockStatic(EngineConfiguration.class);
                var processorMockedConstruction = mockConstruction(
                        ParserProcessor.class);
                var generatorMockedConstruction = mockConstruction(
                        GeneratorProcessor.class)) {
            var engineConfiguration = Mockito.mock(EngineConfiguration.class);
            engineConfigurationMockedStatic.when(
                    () -> EngineConfiguration.loadDirectory(Mockito.any()))
                    .thenReturn(engineConfiguration);

            var listener = new EndpointHotSwapListener(spyEndpointController,
                    spyEndpointHotSwapService);

            listener.endpointChanged(new HotSwapListener.EndpointChangedEvent(
                    Path.of("test-project/target"), null));

            Mockito.verify(spyEndpointController, Mockito.atLeastOnce())
                    .registerEndpoints();
        }
    }

}
