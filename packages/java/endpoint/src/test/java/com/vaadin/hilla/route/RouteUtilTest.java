package com.vaadin.hilla.route;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.server.*;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import com.vaadin.flow.server.menu.AvailableViewInfo;

import javax.annotation.concurrent.NotThreadSafe;

import static java.util.Map.entry;

@NotThreadSafe
public class RouteUtilTest {

    private final RouteUtil routeUtil = new RouteUtil();

    private final ApplicationConfiguration config = Mockito
            .mock(ApplicationConfiguration.class);

    private MockHttpServletRequest request;

    private MockedStatic<ApplicationConfiguration> applicationConfigurationMockedStatic;

    private MockedStatic<MenuRegistry> menuRegistryMockedStatic;

    @Before
    public void setup() throws Exception {
        applicationConfigurationMockedStatic = Mockito
                .mockStatic(ApplicationConfiguration.class);
        applicationConfigurationMockedStatic
                .when(() -> ApplicationConfiguration.get(Mockito.any()))
                .thenReturn(config);
        menuRegistryMockedStatic = Mockito.mockStatic(MenuRegistry.class);

        Mockito.when(config.getMode()).thenReturn(Mode.PRODUCTION_CUSTOM);

        request = new MockHttpServletRequest();
    }

    @After
    public void teardown() throws Exception {
        applicationConfigurationMockedStatic.close();
        menuRegistryMockedStatic.close();
    }

    @Test
    public void test_role_allowed() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_ADMIN");

        AvailableViewInfo config = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN" }, false, "/test", false, false,
                null, null, null, false);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertTrue("Route should be allowed for ADMIN role.",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_role_not_allowed() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_USER");

        AvailableViewInfo config = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN" }, false, "/test", false, false,
                null, null, null, false);
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
                "/test", false, false, null, null, null, false);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertTrue("Request with user principal should be allowed",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_failed() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo config = new AvailableViewInfo("Test", null, true,
                "/test", false, false, null, null, null, false);
        routeUtil.setRoutes(Collections.singletonMap("/test", config));

        Assert.assertFalse("No login should be denied access",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_on_layout() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo pageWithoutLogin = new AvailableViewInfo("Test Page",
                null, false, "/test", false, false, null, null, null, false);

        AvailableViewInfo layoutWithLogin = new AvailableViewInfo("Test Layout",
                null, true, "", false, false, null,
                Collections.singletonList(pageWithoutLogin), null, false);
        routeUtil.setRoutes(Map.ofEntries(entry("/test", pageWithoutLogin),
                entry("", layoutWithLogin)));

        Assert.assertFalse(
                "Access should be denied for layout with login required",
                routeUtil.isRouteAllowed(request));
    }

    @Test
    public void test_login_required_on_page() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo pageWithLogin = new AvailableViewInfo("Test Page",
                null, true, "/test", false, false, null, null, null, false);

        AvailableViewInfo layoutWithoutLogin = new AvailableViewInfo(
                "Test Layout", null, false, "", false, false, null,
                Collections.singletonList(pageWithLogin), null, false);
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
        request.setRequestURI("/context/");
        request.setContextPath("/context");
        request.setUserPrincipal(null);

        AvailableViewInfo config = new AvailableViewInfo("Root", null, false,
                "", false, false, null, null, null, false);
        routeUtil.setRoutes(Collections.singletonMap("", config));

        Assert.assertTrue("Login no required should allow access",
                routeUtil.isRouteAllowed(request));
    }

    /**
     * Verifies that the routes are loaded once from the menu registry in
     * production mode.
     */
    @Test
    public void test_collect_routes_production() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");

        // Initial case: the new route is not available yet
        menuRegistryMockedStatic.when(this::getMenuItems).thenReturn(Map.of());

        Assert.assertFalse(routeUtil.isRouteAllowed(request));
        menuRegistryMockedStatic.verify(this::getMenuItems, Mockito.only());

        menuRegistryMockedStatic.reset();

        // Add a new view
        var clientMenuItems = Map.of("/test", new AvailableViewInfo("Test Page",
                null, false, "/test", false, false, null, null, null, false));
        menuRegistryMockedStatic.when(this::getMenuItems)
                .thenReturn(clientMenuItems);

        Assert.assertFalse(routeUtil.isRouteAllowed(request));
        menuRegistryMockedStatic.verify(this::getMenuItems, Mockito.never());
    }

    /**
     * Verifies that the routes are reloaded from the menu registry in
     * development mode.
     */
    @Test
    public void test_collect_routes_live_reload() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");

        Mockito.when(config.getMode())
                .thenReturn(Mode.DEVELOPMENT_FRONTEND_LIVERELOAD);

        // Initial case: the new route is not available yet
        menuRegistryMockedStatic.when(this::getMenuItems).thenReturn(Map.of());

        Assert.assertFalse(routeUtil.isRouteAllowed(request));
        menuRegistryMockedStatic.verify(this::getMenuItems, Mockito.only());

        menuRegistryMockedStatic.reset();

        // Add a new view
        var clientMenuItems = Map.of("/test", new AvailableViewInfo("Test Page",
                null, false, "/test", false, false, null, null, null, false));
        menuRegistryMockedStatic.when(this::getMenuItems)
                .thenReturn(clientMenuItems);

        Assert.assertTrue(routeUtil.isRouteAllowed(request));
        menuRegistryMockedStatic.verify(this::getMenuItems, Mockito.only());
    }

    private void getMenuItems() {
        MenuRegistry.collectClientMenuItems(Mockito.eq(false),
                Mockito.eq(config), Mockito.isNull());
    }
}
