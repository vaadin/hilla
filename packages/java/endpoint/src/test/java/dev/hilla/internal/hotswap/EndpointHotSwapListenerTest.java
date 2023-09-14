package dev.hilla.internal.hotswap;

import java.io.IOException;
import java.nio.file.Path;

import dev.hilla.EndpointController;
import dev.hilla.EndpointControllerConfiguration;
import dev.hilla.EndpointProperties;
import dev.hilla.ResetEndpointCodeGeneratorInstance;
import dev.hilla.ServletContextTestSetup;
import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserProcessor;
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
        JacksonProperties.class, EndpointController.class })
@ContextConfiguration(classes = { ResetEndpointCodeGeneratorInstance.class,
        EndpointControllerConfiguration.class })
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
