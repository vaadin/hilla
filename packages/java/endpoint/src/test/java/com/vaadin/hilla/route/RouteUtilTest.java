package com.vaadin.hilla.route;

import java.security.Principal;

import com.vaadin.hilla.route.records.ClientViewConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

public class RouteUtilTest {

    private final RouteUtil endpointUtil;
    private final ClientRouteRegistry registry;

    public RouteUtilTest() {
        registry = new ClientRouteRegistry();
        this.endpointUtil = new RouteUtil(registry);
    }

    @Before
    public void setup() throws Exception {
        registry.clearRoutes();
    }

    @Test
    public void test_role_allowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_ADMIN");

        registry.addRoute("/test",
                new ClientViewConfig("Test", new String[] { "ROLE_ADMIN" },
                        false, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_role_not_allowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_USER");

        registry.addRoute("/test",
                new ClientViewConfig("Test", new String[] { "ROLE_ADMIN" },
                        false, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }

    @Test
    public void test_login_required() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(Mockito.mock(Principal.class));

        registry.addRoute("/test", new ClientViewConfig("Test", null, true,
                "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_login_required_failed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        registry.addRoute("/test", new ClientViewConfig("Test", null, true,
                "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }
}
