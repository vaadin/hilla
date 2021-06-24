package com.vaadin.fusion;

import com.vaadin.fusion.auth.VaadinConnectAccessChecker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = VaadinEndpointProperties.class)
@ContextConfiguration(classes = VaadinConnectControllerConfiguration.class)
@RunWith(SpringRunner.class)
public class VaadinConnectControllerConfigurationTest {

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private VaadinConnectAccessChecker vaadinConnectAccessChecker;

    @Test
    public void dependenciesAvailable() {
        Assert.assertNotNull(endpointRegistry);
        Assert.assertNotNull(vaadinConnectAccessChecker);
    }
}
