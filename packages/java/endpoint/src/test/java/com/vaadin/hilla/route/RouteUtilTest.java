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
package com.vaadin.hilla.route;

import static java.util.Map.entry;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.server.Mode;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.server.menu.RouteParamType;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

@NotThreadSafe
public class RouteUtilTest {

    private final RouteUtil routeUtil = new RouteUtil();

    private final ApplicationConfiguration config = Mockito
            .mock(ApplicationConfiguration.class);

    private MockHttpServletRequest request;

    private MockedStatic<ApplicationConfiguration> applicationConfigurationMockedStatic;

    private MockedStatic<MenuRegistry> menuRegistryMockedStatic;

    @Before
    public void setup() {
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
    public void teardown() {
        applicationConfigurationMockedStatic.close();
        menuRegistryMockedStatic.close();
    }

    @Test
    public void isAnonymousRoute_loginNotRequired_returnsTrue() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", new String[0], false, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertTrue(routeUtil.isAnonymousRoute(request));
    }

    @Test
    public void isAnonymousRoute_loginRequired_returnsFalse() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", new String[0], true, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertFalse(routeUtil.isAnonymousRoute(request));
    }

    @Test
    public void isAnonymousRoute_invalidRoute_returnFalse() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        routeUtil.setRoutes(Collections.emptyMap());
        Assert.assertFalse(routeUtil.isAnonymousRoute(request));
    }

    @Test
    public void isSecuredRoute_loginNotRequired_returnsFalse() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", new String[0], false, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertFalse(routeUtil.isSecuredRoute(request));
    }

    @Test
    public void isSecuredRoute_loginRequired_returnsTrue() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", new String[0], true, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertTrue(routeUtil.isSecuredRoute(request));
    }

    @Test
    public void isSecuredRoute_invalidRoute_returnFalse() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        routeUtil.setRoutes(Collections.emptyMap());
        Assert.assertFalse(routeUtil.isSecuredRoute(request));
    }

    @Test
    public void isSecuredRoute_withRequiredParameter_returnsTrue() {
        request.setRequestURI("/context/test/12345");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test Item", new String[0], true,
                "/test/:testId", false, false, null, null,
                Map.of(":testId", RouteParamType.REQUIRED), false, "");
        routeUtil.setRoutes(Collections.singletonMap(info.route(), info));
        Assert.assertTrue(routeUtil.isSecuredRoute(request));
    }

    @Test
    public void isSecuredRoute_withOptionalParameter_returnsTrue() {
        var info = new AvailableViewInfo("Test Item", new String[0], true,
                "/test/:testId?/inner", false, false, null, null,
                Map.of(":testId", RouteParamType.REQUIRED), false, "");
        routeUtil.setRoutes(Collections.singletonMap(info.route(), info));

        request.setRequestURI("/context/test/12345/inner");
        request.setContextPath("/context");
        Assert.assertTrue(routeUtil.isSecuredRoute(request));

        request.setRequestURI("/context/test/inner");
        Assert.assertTrue(routeUtil.isSecuredRoute(request));

        request.setRequestURI("/context/test");
        Assert.assertFalse(routeUtil.isSecuredRoute(request));
    }

    @Test
    public void getAllowedAuthorities_noRoles_returnsEmptySet() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", new String[0], false, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertTrue(routeUtil.getAllowedAuthorities(request).isEmpty());
    }

    @Test
    public void getAllowedAuthorities_hasRoles_returnsRolesSet() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN", "ROLE_USER" }, false, "/test",
                false, false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertEquals(2, routeUtil.getAllowedAuthorities(request).size());
        Assert.assertTrue(routeUtil.getAllowedAuthorities(request)
                .contains("ROLE_ADMIN"));
        Assert.assertTrue(
                routeUtil.getAllowedAuthorities(request).contains("ROLE_USER"));
    }

    @Test
    public void getAllowedAuthorities_nullRoles_returnsEmptySet() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        var info = new AvailableViewInfo("Test", null, false, "/test", false,
                false, null, null, null, false, "");
        routeUtil.setRoutes(Collections.singletonMap("/test", info));
        Assert.assertTrue(routeUtil.getAllowedAuthorities(request).isEmpty());
    }

    @Test
    public void getAllowedAuthorities_invalidRoute_returnEmptySet() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        routeUtil.setRoutes(Collections.emptyMap());
        Assert.assertTrue(routeUtil.getAllowedAuthorities(request).isEmpty());
    }

    @Test
    public void test_role_allowed() {
        request.setRequestURI("/context/test");
        request.setContextPath("/context");
        request.addUserRole("ROLE_ADMIN");

        AvailableViewInfo config = new AvailableViewInfo("Test",
                new String[] { "ROLE_ADMIN" }, false, "/test", false, false,
                null, null, null, false, "");
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
                null, null, null, false, "");
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
                "/test", false, false, null, null, null, false, "");
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
                "/test", false, false, null, null, null, false, "");
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
                null, false, "/test", false, false, null, null, null, false,
                "");

        AvailableViewInfo layoutWithLogin = new AvailableViewInfo("Test Layout",
                null, true, "", false, false, null,
                Collections.singletonList(pageWithoutLogin), null, false, "");
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
                null, true, "/test", false, false, null, null, null, false, "");

        AvailableViewInfo layoutWithoutLogin = new AvailableViewInfo(
                "Test Layout", null, false, "", false, false, null,
                Collections.singletonList(pageWithLogin), null, false, "");
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
                "", false, false, null, null, null, false, "");
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
        var clientMenuItems = Map.of("/test",
                new AvailableViewInfo("Test Page", null, false, "/test", false,
                        false, null, null, null, false, ""));
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
        var clientMenuItems = Map.of("/test",
                new AvailableViewInfo("Test Page", null, false, "/test", false,
                        false, null, null, null, false, ""));
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
