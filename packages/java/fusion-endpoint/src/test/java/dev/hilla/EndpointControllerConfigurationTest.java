package dev.hilla;

import dev.hilla.auth.EndpointAccessChecker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class })
@ContextConfiguration(classes = EndpointControllerConfiguration.class)
@RunWith(SpringRunner.class)
public class EndpointControllerConfigurationTest {

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private EndpointAccessChecker EndpointAccessChecker;

    @Test
    public void dependenciesAvailable() {
        Assert.assertNotNull(endpointRegistry);
        Assert.assertNotNull(EndpointAccessChecker);
    }
}
