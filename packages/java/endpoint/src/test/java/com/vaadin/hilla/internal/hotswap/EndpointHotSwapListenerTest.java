package com.vaadin.hilla.internal.hotswap;

import java.io.IOException;
import java.nio.file.Path;

import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.EndpointController;
import com.vaadin.hilla.EndpointControllerConfiguration;
import com.vaadin.hilla.EndpointProperties;
import com.vaadin.hilla.ServletContextTestSetup;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.mockConstruction;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, Jackson2ObjectMapperBuilder.class,
        JacksonProperties.class, EndpointController.class,
        ApplicationContextProvider.class })
@ContextConfiguration(classes = { EndpointControllerConfiguration.class })
public class EndpointHotSwapListenerTest {

    @SpyBean
    private EndpointController endpointController;

    @MockBean
    private EndpointHotSwapService endpointHotSwapService;

    @Test
    public void endpointChangedIsCalled_endpointCodeGeneratorUpdateIsCalled()
            throws IOException {
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

            var listener = new EndpointHotSwapListener(endpointHotSwapService);

            Mockito.clearInvocations(endpointController);
            listener.endpointChanged(new HotSwapListener.EndpointChangedEvent(
                    Path.of("test-project/target"), null));

            Mockito.verify(endpointController, Mockito.atLeastOnce())
                    .registerEndpoints();
        }
    }

}
