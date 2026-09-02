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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.hilla.EndpointInvocationException.EndpointHttpException;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import com.vaadin.hilla.auth.EndpointInvocation;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, JacksonProperties.class,
        EndpointController.class,
        EndpointInvokerTest.AuthorizationEventPublisherTestConfiguration.class })
@ContextConfiguration(classes = { EndpointControllerConfiguration.class })
@RunWith(SpringRunner.class)
public class EndpointInvokerTest {

    private final ObjectNode body = null;

    @Autowired
    private ApplicationContext applicationContext;

    @Mock
    private EndpointAccessChecker endpointAccessChecker;

    @Mock
    private EndpointNameChecker endpointNameChecker;

    @Mock
    private HttpServletRequest requestMock;

    @Mock
    private Principal principal;

    @Mock
    private ExplicitNullableTypeChecker explicitNullableTypeChecker;

    @Mock
    private ServletContext servletContext;

    @Autowired
    private RecordingAuthorizationEventPublisher authorizationEventPublisher;

    private EndpointInvoker endpointInvoker;
    private EndpointRegistry endpointRegistry;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authorizationEventPublisher.events.clear();
        when(requestMock.getUserPrincipal()).thenReturn(principal);

        when(endpointNameChecker.check(any())).thenReturn(null);

        endpointRegistry = new EndpointRegistry(endpointNameChecker);

        endpointInvoker = new EndpointInvoker(applicationContext,
                new JacksonObjectMapperFactory.Json().build(),
                explicitNullableTypeChecker, servletContext, endpointRegistry) {
            protected EndpointAccessChecker getAccessChecker() {
                return endpointAccessChecker;
            }
        };
    }

    @Test
    public void when_invokedMethod_isDeclaredIn_Endpoint_accessCheckingIsDoneBasedOn_EndpointMethod()
            throws Exception {

        @EndpointExposed
        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @Endpoint
        class TestEndpoint extends ParentEndpoint {
            public void test() {
            }
        }

        TestEndpoint test = new TestEndpoint();

        endpointRegistry.registerEndpoint(test);

        endpointInvoker.invoke("TestEndpoint", "test", body, principal,
                requestMock::isUserInRole);

        Mockito.verify(endpointAccessChecker, Mockito.times(1))
                .check(any(Method.class), any(), any());
        Mockito.verify(endpointAccessChecker, Mockito.times(0))
                .check(any(Class.class), any(), any());
    }

    @Test
    public void when_invokedMethod_isDeclaredIn_EndpointExposed_accessCheckingIsDoneBasedOn_EndpointClass()
            throws Exception {

        @EndpointExposed
        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @Endpoint
        class TestEndpoint extends ParentEndpoint {
            public void test() {
            }
        }

        TestEndpoint test = new TestEndpoint();

        endpointRegistry.registerEndpoint(test);

        endpointInvoker.invoke("TestEndpoint", "sayhello", body, principal,
                requestMock::isUserInRole);

        Mockito.verify(endpointAccessChecker, Mockito.times(0))
                .check(any(Method.class), any(), any());
        Mockito.verify(endpointAccessChecker, Mockito.times(1))
                .check(eq(TestEndpoint.class), any(), any());
    }

    @Test
    public void when_invokedMethod_isOverriddenIn_Endpoint_accessCheckingIsDoneBasedOn_EndpointMethod()
            throws Exception {

        @EndpointExposed
        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @Endpoint
        class TestEndpoint extends ParentEndpoint {

            @Override
            public String sayHello() {
                return "Hello from TestEndpoint";
            }

            public void test() {
            }
        }

        TestEndpoint test = new TestEndpoint();

        endpointRegistry.registerEndpoint(test);

        endpointInvoker.invoke("TestEndpoint", "sayhello", body, principal,
                requestMock::isUserInRole);

        Mockito.verify(endpointAccessChecker, Mockito.times(1))
                .check(eq(test.getClass().getMethod("sayHello")), any(), any());
        Mockito.verify(endpointAccessChecker, Mockito.times(0)).check(
                eq(test.getClass().getSuperclass().getMethod("sayHello")),
                any(), any());
        Mockito.verify(endpointAccessChecker, Mockito.times(0))
                .check(any(Class.class), any(), any());
    }

    static class TeapotException extends EndpointHttpException {
        TeapotException() {
            super("I'm a teapot");
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.I_AM_A_TEAPOT;
        }
    }

    @Test
    public void httpExceptionIsRethrown() {
        @Endpoint
        class TestEndpoint {

            public String sayHello() throws TeapotException {
                throw new TeapotException();
            }
        }

        TestEndpoint test = new TestEndpoint();

        endpointRegistry.registerEndpoint(test);

        var ex = assertThrows(TeapotException.class,
                () -> endpointInvoker.invoke("TestEndpoint", "sayhello", body,
                        principal, requestMock::isUserInRole));
        assertEquals(418, ex.getHttpStatusCode());
        assertEquals("I'm a teapot", ex.getMessage());
    }

    @Endpoint
    static class SecuredEndpoint {
        public void secured() {
        }
    }

    @Test
    public void when_accessIsDenied_authorizationDeniedEventIsPublished()
            throws Exception {
        endpointRegistry.registerEndpoint(new SecuredEndpoint());
        when(endpointAccessChecker.check(any(Method.class), any(), any()))
                .thenReturn(EndpointAccessChecker.ACCESS_DENIED_MSG);

        assertThrows(EndpointHttpException.class,
                () -> endpointInvoker.invoke("SecuredEndpoint", "secured", body,
                        principal, requestMock::isUserInRole));

        assertEquals(1, authorizationEventPublisher.events.size());
        var event = authorizationEventPublisher.events.get(0);
        assertFalse(event.result().isGranted());
        assertTrue(
                "the published object should be a MethodInvocation so that "
                        + "existing Spring Security listeners can consume it",
                event.object() instanceof MethodInvocation);
        var invocation = (EndpointInvocation) event.object();
        assertEquals("SecuredEndpoint", invocation.getEndpointName());
        assertEquals("secured", invocation.getMethodName());
        assertEquals(SecuredEndpoint.class.getMethod("secured"),
                invocation.getMethod());
        assertEquals("the request payload must not be exposed in the event", 0,
                invocation.getArguments().length);
        assertThrows(UnsupportedOperationException.class, invocation::proceed);
    }

    @Test
    public void when_accessIsAllowed_noAuthorizationEventIsPublished()
            throws Exception {
        endpointRegistry.registerEndpoint(new SecuredEndpoint());

        endpointInvoker.invoke("SecuredEndpoint", "secured", body, principal,
                requestMock::isUserInRole);

        assertEquals(0, authorizationEventPublisher.events.size());
    }

    record PublishedEvent(Supplier<Authentication> authentication,
            Object object, AuthorizationResult result) {
    }

    static class RecordingAuthorizationEventPublisher
            implements AuthorizationEventPublisher {
        private final List<PublishedEvent> events = new ArrayList<>();

        @Override
        public <T> void publishAuthorizationEvent(
                Supplier<Authentication> authentication, T object,
                AuthorizationResult result) {
            events.add(new PublishedEvent(authentication, object, result));
        }
    }

    static class AuthorizationEventPublisherTestConfiguration {
        @Bean
        RecordingAuthorizationEventPublisher authorizationEventPublisher() {
            return new RecordingAuthorizationEventPublisher();
        }
    }

}
