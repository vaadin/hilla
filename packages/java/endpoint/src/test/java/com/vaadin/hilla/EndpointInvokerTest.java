package com.vaadin.hilla;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.security.Principal;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointInvocationException.EndpointHttpException;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { ServletContextTestSetup.class,
        EndpointProperties.class, Jackson2ObjectMapperBuilder.class,
        JacksonProperties.class, EndpointController.class })
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

    private EndpointInvoker endpointInvoker;
    private EndpointRegistry endpointRegistry;

    @Before
    public void setUp() {
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

    @Test
    public void httpExceptionIsRethrown() {
        @Endpoint
        class TestEndpoint {

            public String sayHello() throws EndpointHttpException {
                throw new EndpointHttpException(418, "I'm a teapot");
            }
        }

        TestEndpoint test = new TestEndpoint();

        endpointRegistry.registerEndpoint(test);

        var ex = assertThrows(EndpointHttpException.class,
                () -> endpointInvoker.invoke("TestEndpoint", "sayhello", body,
                        principal, requestMock::isUserInRole));
        assertEquals(418, ex.getHttpStatusCode());
        assertEquals("I'm a teapot", ex.getMessage());
    }

}
