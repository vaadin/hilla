/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.vaadin.hilla.auth.EndpointAccessChecker;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, JacksonProperties.class,
        EndpointController.class })
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
