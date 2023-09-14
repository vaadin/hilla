package dev.hilla;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import dev.hilla.auth.EndpointAccessChecker;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, Jackson2ObjectMapperBuilder.class,
        JacksonProperties.class, EndpointController.class })
@ContextConfiguration(classes = { ResetEndpointCodeGeneratorInstance.class,
        EndpointControllerConfiguration.class })
@RunWith(SpringRunner.class)
public class EndpointControllerConfigurationTest {

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private EndpointAccessChecker endpointAccessChecker;

    @Test
    public void dependenciesAvailable() {
        Assert.assertNotNull(endpointRegistry);
        Assert.assertNotNull(endpointAccessChecker);
    }
}
