package com.vaadin.hilla;

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
    public void testEndpointInvokerUsesQualifiedObjectMapper()
            throws NoSuchFieldException, IllegalAccessException {
        var endpointInvoker = context.getBean(EndpointInvoker.class);
        var objectMapper = context.getBean("hillaEndpointObjectMapper");

        Assert.assertNotNull("EndpointInvoker should not be null",
                endpointInvoker);
        Assert.assertNotNull("hillaEndpointObjectMapper should not be null",
                objectMapper);

        var field = EndpointInvoker.class
                .getDeclaredField("endpointObjectMapper");
        field.setAccessible(true);

        Assert.assertSame(
                "EndpointInvoker should use the qualified hillaEndpointObjectMapper",
                objectMapper, field.get(endpointInvoker));
    }
}
