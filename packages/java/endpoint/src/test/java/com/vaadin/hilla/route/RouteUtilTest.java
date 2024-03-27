package com.vaadin.hilla.route;

import java.security.Principal;
import java.util.List;

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

        ClientViewConfig config = new ClientViewConfig();
        config.setTitle("Test");
        config.setRolesAllowed(new String[] { "ROLE_ADMIN" });
        config.setLoginRequired(false);
        config.setRoute("/test");
        config.setLazy(false);
        config.setAutoRegistered(false);
        config.setMenu(null);
        config.setChildren(null);
        config.setRouteParameters(null);
        registry.addRoute("/test", config);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_role_not_allowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_USER");

        ClientViewConfig config = new ClientViewConfig();
        config.setTitle("Test");
        config.setRolesAllowed(new String[] { "ROLE_ADMIN" });
        config.setLoginRequired(false);
        config.setRoute("/test");
        config.setLazy(false);
        config.setAutoRegistered(false);
        config.setMenu(null);
        config.setChildren(null);
        config.setRouteParameters(null);
        registry.addRoute("/test", config);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }

    @Test
    public void test_login_required() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(Mockito.mock(Principal.class));

        ClientViewConfig config = new ClientViewConfig();
        config.setTitle("Test");
        config.setRolesAllowed(null);
        config.setLoginRequired(true);
        config.setRoute("/test");
        config.setLazy(false);
        config.setAutoRegistered(false);
        config.setMenu(null);
        config.setChildren(null);
        config.setRouteParameters(null);
        registry.addRoute("/test", config);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }

    @Test
    public void test_login_required_failed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        ClientViewConfig config = new ClientViewConfig();
        config.setTitle("Test");
        config.setRolesAllowed(null);
        config.setLoginRequired(true);
        config.setRoute("/test");
        config.setLazy(false);
        config.setAutoRegistered(false);
        config.setMenu(null);
        config.setChildren(null);
        config.setRouteParameters(null);
        registry.addRoute("/test", config);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }

    @Test
    public void test_login_required_on_layout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        var pageWithoutLogin = new ClientViewConfig();
        pageWithoutLogin.setTitle("Test Page");
        pageWithoutLogin.setRolesAllowed(null);
        pageWithoutLogin.setLoginRequired(false);
        pageWithoutLogin.setRoute("");
        pageWithoutLogin.setLazy(false);
        pageWithoutLogin.setAutoRegistered(false);
        pageWithoutLogin.setMenu(null);
        pageWithoutLogin.setChildren(null);
        pageWithoutLogin.setRouteParameters(null);

        var layoutWithLogin = new ClientViewConfig();
        layoutWithLogin.setTitle("Test Layout");
        layoutWithLogin.setRolesAllowed(null);
        layoutWithLogin.setLoginRequired(true);
        layoutWithLogin.setRoute("/test");
        layoutWithLogin.setLazy(false);
        layoutWithLogin.setAutoRegistered(false);
        layoutWithLogin.setMenu(null);
        layoutWithLogin.setChildren(List.of(pageWithoutLogin));
        layoutWithLogin.setRouteParameters(null);

        pageWithoutLogin.setParent(layoutWithLogin);

        registry.addRoute("/test", pageWithoutLogin);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }

    @Test
    public void test_login_required_on_page() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        var pageWithLogin = new ClientViewConfig();
        pageWithLogin.setTitle("Test Page");
        pageWithLogin.setRolesAllowed(null);
        pageWithLogin.setLoginRequired(true);
        pageWithLogin.setRoute("");
        pageWithLogin.setLazy(false);
        pageWithLogin.setAutoRegistered(false);
        pageWithLogin.setMenu(null);
        pageWithLogin.setChildren(null);
        pageWithLogin.setRouteParameters(null);

        var layoutWithoutLogin = new ClientViewConfig();
        layoutWithoutLogin.setTitle("Test Layout");
        layoutWithoutLogin.setRolesAllowed(null);
        layoutWithoutLogin.setLoginRequired(false);
        layoutWithoutLogin.setRoute("/test");
        layoutWithoutLogin.setLazy(false);
        layoutWithoutLogin.setAutoRegistered(false);
        layoutWithoutLogin.setMenu(null);
        layoutWithoutLogin.setChildren(List.of(pageWithLogin));
        layoutWithoutLogin.setRouteParameters(null);

        pageWithLogin.setParent(layoutWithoutLogin);

        registry.addRoute("/test", pageWithLogin);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertFalse(actual);
    }

    /**
     * Verifies that the root route is allowed when login is not required,
     * despite the mismatch between "/" and "".
     */
    @Test
    public void test_login_not_required_on_root() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        ClientViewConfig config = new ClientViewConfig();
        config.setTitle("Root");
        config.setRolesAllowed(null);
        config.setLoginRequired(false);
        config.setRoute("");
        config.setLazy(false);
        config.setAutoRegistered(false);
        config.setMenu(null);
        config.setChildren(null);
        config.setRouteParameters(null);
        registry.addRoute("", config);

        boolean actual = endpointUtil.isRouteAllowed(request);
        Assert.assertTrue(actual);
    }
}
