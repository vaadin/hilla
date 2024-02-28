package com.vaadin.hilla.route;

import com.vaadin.hilla.route.records.ClientViewConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    public void test_isRouteRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{"admin"}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteRequest(request);
        Assert.assertTrue(actual);
    }
    @Test
    public void test_isAnonymousRoute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isAnonymousRoute(request);
        Assert.assertTrue(actual);
    }
    @Test
    public void test_isAnonymousRoute_roleAnonymous() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{"Anonymous"}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isAnonymousRoute(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_isAnonymousRoute_false() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{"admin"}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isAnonymousRoute(request);
        Assert.assertFalse(actual);
    }

    @Test
    public void test_isRouteAllowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_ADMIN");

        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{"ROLE_ADMIN"}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_isRouteAllowed_false() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
            "/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_USER");

        registry.addRoute("/test", new ClientViewConfig("Test", new String[]{"ROLE_ADMIN"}, "/test", false, false, null, null, null));

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }
}
