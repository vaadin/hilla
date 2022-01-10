package com.vaadin.fusion.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.fusion.Endpoint;

public class FusionEndpointsValidatorTest {

    @Endpoint
    public static class WithConnectEndpoint {
    }

    public static class WithoutConnectEndpoint {
    }

    private Set<Class<?>> classes;
    private ServletContext servletContext;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        classes = new HashSet<Class<?>>();
        servletContext = Mockito.mock(ServletContext.class);
    }

    @Test
    public void should_start_when_spring_in_classpath() throws Exception {
        FusionEndpointsValidator validator = new FusionEndpointsValidator();
        classes.add(WithConnectEndpoint.class);
        validator.process(classes, servletContext);
    }

    @Test
    public void should_trow_when_spring_not_in_classpath() throws Exception {
        exception.expect(ServletException.class);
        FusionEndpointsValidator validator = new FusionEndpointsValidator();
        validator.setClassToCheck("foo.bar.Baz");
        classes.add(WithConnectEndpoint.class);
        validator.process(classes, servletContext);

    }

    @Test
    public void should_start_when_no_endpoints_and_spring_not_in_classpath()
            throws Exception {
        FusionEndpointsValidator validator = new FusionEndpointsValidator();
        classes.add(WithoutConnectEndpoint.class);
        validator.process(classes, servletContext);
    }

    @Test
    public void should_start_when_CDI_environment() throws Exception {
        FusionEndpointsValidator validator = new FusionEndpointsValidator();
        classes = null;
        validator.process(classes, servletContext);
    }
}
