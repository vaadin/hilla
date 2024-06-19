package com.vaadin.hilla.route;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import com.vaadin.flow.server.menu.AvailableViewInfo;

import static java.util.Map.entry;

public class RouteUtilTest {

    private final RouteUtil routeUtil;

    public RouteUtilTest() {
        this.routeUtil = new RouteUtil();
    }

    @Before
    public void setup() throws Exception {
        routeUtil.setRoutes(null);
    }

    @Test
    public void test_role_allowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_ADMIN");

        AvailableViewInfo config = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN" }, false, "/test", false, false,
                null, null, null);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertTrue("Route should be allowed for ADMIN role.",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_role_not_allowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_USER");

        AvailableViewInfo config = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN" }, false, "/test", false, false,
                null, null, null);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertFalse("USER role should not allow ADMIN route.",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(Mockito.mock(Principal.class));

        AvailableViewInfo config = new AvailableViewInfo("Test", null, true,
                "/test", false, false, null, null, null);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertTrue("Request with user principal should be allowed",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_failed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo config = new AvailableViewInfo("Test", null, true,
                "/test", false, false, null, null, null);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertFalse("No login should be denied access",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_on_layout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo pageWithoutLogin = new AvailableViewInfo("Test Page",
                null, false, "/test", false, false, null, null, null);

        AvailableViewInfo layoutWithLogin = new AvailableViewInfo("Test Layout",
                null, true, "", false, false, null,
                Collections.singletonList(pageWithoutLogin), null);
        routeUtil.setRoutes(Map.ofEntries(entry("/test", pageWithoutLogin),
                entry("", layoutWithLogin)));

        Assert.assertFalse(
                "Access should be denied for layout with login required",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_on_page() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo pageWithLogin = new AvailableViewInfo("Test Page",
                null, true, "/test", false, false, null, null, null);

        AvailableViewInfo layoutWithoutLogin = new AvailableViewInfo(
                "Test Layout", null, false, "", false, false, null,
                Collections.singletonList(pageWithLogin), null);
        routeUtil.setRoutes(Map.ofEntries(entry("/test", pageWithLogin),
                entry("", layoutWithoutLogin)));

        Assert.assertFalse("Access should be denied for page requiring login",
                routeUtil.isRouteAllowed(request));
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

        AvailableViewInfo config = new AvailableViewInfo("Root", null, false,
                "", false, false, null, null, null);
        routeUtil.setRoutes(Collections.singletonMap("", config));

        Assert.assertTrue("Login no required should allow access",
                routeUtil.isRouteAllowed(request));
    }
}
