package com.vaadin.fusion;

import com.vaadin.fusion.auth.FusionAccessChecker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        FusionEndpointProperties.class })
@ContextConfiguration(classes = FusionControllerConfiguration.class)
@RunWith(SpringRunner.class)
public class FusionControllerConfigurationTest {

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private FusionAccessChecker fusionAccessChecker;

    @Test
    public void dependenciesAvailable() {
        Assert.assertNotNull(endpointRegistry);
        Assert.assertNotNull(fusionAccessChecker);
    }
}
