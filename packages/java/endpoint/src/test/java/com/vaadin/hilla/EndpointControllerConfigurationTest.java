package com.vaadin.hilla;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.vaadin.hilla.auth.EndpointAccessChecker;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, Jackson2ObjectMapperBuilder.class,
        JacksonProperties.class, EndpointController.class })
@ContextConfiguration(classes = { EndpointControllerConfiguration.class })
@RunWith(SpringRunner.class)
public class EndpointControllerConfigurationTest {

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private EndpointAccessChecker endpointAccessChecker;

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    public void dependenciesAvailable() {
        Assert.assertNotNull(endpointRegistry);
        Assert.assertNotNull(endpointAccessChecker);
    }

    @Test
    public void testEndpointInvokerDependsOnHillaObjectMapper() {
        // Fetch the BeanDefinition for endpointInvoker
        var endpointInvokerDefinition = context.getBeanFactory()
                .getBeanDefinition("endpointInvoker");

        var dependsOn = endpointInvokerDefinition.getDependsOn();

        Assert.assertNotNull("dependsOn should not be null", dependsOn);
        Assert.assertTrue(
                "endpointInvoker should depend on hillaEndpointObjectMapper",
                Arrays.asList(dependsOn).contains("hillaEndpointObjectMapper"));
    }
}
