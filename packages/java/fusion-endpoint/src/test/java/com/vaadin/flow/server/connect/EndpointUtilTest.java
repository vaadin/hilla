package com.vaadin.flow.server.connect;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.connect.auth.VaadinConnectAccessChecker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { EndpointUtil.class, VaadinEndpointProperties.class,
        EndpointRegistry.class, EndpointNameChecker.class,
        VaadinConnectAccessChecker.class })
@RunWith(SpringRunner.class)
public class EndpointUtilTest {

    @Autowired
    private EndpointUtil endpointUtil;
    @Autowired
    private EndpointRegistry registry;

    private static final Class<?>[] endpointClasses = new Class<?>[] {
            TestEndpoints.AnonymousAllowedEndpoint.class,
            TestEndpoints.DenyAllEndpoint.class,
            TestEndpoints.NoAnnotationEndpoint.class,
            TestEndpoints.PermitAllEndpoint.class,
            TestEndpoints.RolesAllowedAdminEndpoint.class,
            TestEndpoints.RolesAllowedUserEndpoint.class };

    private static final String[] endpointMethods = new String[] {
            "noannotation", "anonymousallowed", "permitall", "denyall",
            "rolesalloweduser", "rolesallowedadmin" };

    private static final String[] endpointNames = Stream.of(endpointClasses)
            .map(cls -> cls.getSimpleName().toLowerCase(Locale.ENGLISH))
            .toArray(String[]::new);

    @Before
    public void setup() throws Exception {
        for (int i = 0; i < endpointClasses.length; i++) {
            registry.registerEndpoint(endpointClasses[i].newInstance());
        }
    }

    @Test
    public void endpointLikeRequest() {
        testPath("/connect/AnonymousAllowedEndpoint/permitall", true);
        testPath("/connect/AnonymousAllowedEndpoint/foo", false);

        for (String endpointName : endpointNames) {
            for (String endpointMethod : endpointMethods) {
                testPath("/connect/" + endpointName + "/" + endpointMethod,
                        true);
            }
        }
    }

    @Test
    public void isAnonymousEndpoint() {
        verifyAnonymousAccessAllowed("AnonymousAllowedEndpoint", "noannotation",
                "anonymousallowed");
        verifyAnonymousAccessAllowed("DenyAllEndpoint", "anonymousallowed");
        verifyAnonymousAccessAllowed("NoAnnotationEndpoint",
                "anonymousallowed");
        verifyAnonymousAccessAllowed("DenyAllEndpoint", "anonymousallowed");
        verifyAnonymousAccessAllowed("PermitAllEndpoint", "anonymousallowed");
        verifyAnonymousAccessAllowed("RolesAllowedAdminEndpoint",
                "anonymousallowed");
        verifyAnonymousAccessAllowed("RolesAllowedUserEndpoint",
                "anonymousallowed");
    }

    private void verifyAnonymousAccessAllowed(String endpointName,
            String... expectedAnonMethods) {
        List<String> expectedAnonList = Arrays.asList(expectedAnonMethods);
        for (String endpointMethod : endpointMethods) {
            verifyEndpointPathIsAnonymous(
                    "/connect/" + endpointName + "/" + endpointMethod,
                    expectedAnonList.contains(endpointMethod));
        }
    }

    private void verifyEndpointPathIsAnonymous(String path, boolean expected) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn(path);
        Assert.assertTrue(endpointUtil.isEndpointRequest(request));
        Assert.assertEquals(
                "Expected endpoint " + path + " to "
                        + (expected ? "be an anonymous endpoint"
                                : "not be an anonymous endpoint"),
                expected, endpointUtil.isAnonymousEndpoint(request));
    }

    @Test
    public void nonEndpointRequest() {
        testPath("/", false);
        testPath("/VAADIN", false);
        testPath("/vaadinServlet", false);
        testPath("/foo/bar", false);
    }

    private void testPath(String path, boolean expected) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn(path);
        Mockito.when(request.getRequestURI()).thenReturn(path);
        Assert.assertEquals(expected, endpointUtil.isEndpointRequest(request));

        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServletPath()).thenReturn(path);
        Mockito.when(request.getRequestURI()).thenReturn(path);
        Assert.assertEquals(expected, endpointUtil.isEndpointRequest(request));

    }

}
