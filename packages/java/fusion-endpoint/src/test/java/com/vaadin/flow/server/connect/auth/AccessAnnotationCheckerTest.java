package com.vaadin.flow.server.connect.auth;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.connect.TestEndpoints;
import com.vaadin.flow.server.connect.TestEndpoints.AnonymousAllowedEndpoint;
import com.vaadin.flow.server.connect.TestEndpoints.DenyAllEndpoint;
import com.vaadin.flow.server.connect.TestEndpoints.NoAnnotationEndpoint;
import com.vaadin.flow.server.connect.TestEndpoints.PermitAllEndpoint;
import com.vaadin.flow.server.connect.TestEndpoints.RolesAllowedAdminEndpoint;
import com.vaadin.flow.server.connect.TestEndpoints.RolesAllowedUserEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

public class AccessAnnotationCheckerTest {
    public static final Class<?>[] ENDPOINT_CLASSES = new Class<?>[] {
            TestEndpoints.AnonymousAllowedEndpoint.class,
            TestEndpoints.DenyAllEndpoint.class,
            TestEndpoints.NoAnnotationEndpoint.class,
            TestEndpoints.PermitAllEndpoint.class,
            TestEndpoints.RolesAllowedAdminEndpoint.class,
            TestEndpoints.RolesAllowedUserEndpoint.class };

    public static final String[] ENDPOINT_METHODS = new String[] {
            "noAnnotation", "anonymousAllowed", "permitAll", "denyAll",
            "rolesAllowedUser", "rolesAllowedAdmin", "rolesAllowedUserAdmin" };

    public static final String[] ENDPOINT_NAMES = Stream.of(ENDPOINT_CLASSES)
            .map(cls -> cls.getSimpleName().toLowerCase(Locale.ENGLISH))
            .toArray(String[]::new);;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private AccessAnnotationChecker accessAnnotationChecker;

    @Before
    public void before() {
        accessAnnotationChecker = new AccessAnnotationChecker();
    }

    @Test
    public void should_Throw_When_PrivateMethodIsPassed() throws Exception {
        class Test {
            private void test() {
            }
        }

        Method method = Test.class.getDeclaredMethod("test");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(method.toString());
        accessAnnotationChecker.getSecurityTarget(method);
    }

    @Test
    public void should_ReturnEnclosingClassAsSecurityTarget_When_NoSecurityAnnotationsPresent()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        assertEquals(Test.class, accessAnnotationChecker
                .getSecurityTarget(Test.class.getMethod("test")));
    }

    @Test
    public void should_ReturnEnclosingClassAsSecurityTarget_When_OnlyClassHasSecurityAnnotations()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {
            }
        }
        assertEquals(Test.class, accessAnnotationChecker
                .getSecurityTarget(Test.class.getMethod("test")));
    }

    @Test
    public void should_ReturnMethodAsSecurityTarget_When_OnlyMethodHasSecurityAnnotations()
            throws Exception {
        class Test {
            @AnonymousAllowed
            public void test() {
            }
        }
        Method securityMethod = Test.class.getMethod("test");
        assertEquals(securityMethod,
                accessAnnotationChecker.getSecurityTarget(securityMethod));
    }

    @Test
    public void should_ReturnMethodAsSecurityTarget_When_BothClassAndMethodHaveSecurityAnnotations()
            throws Exception {
        @AnonymousAllowed
        class Test {
            @AnonymousAllowed
            public void test() {
            }
        }
        Method securityMethod = Test.class.getMethod("test");
        assertEquals(securityMethod,
                accessAnnotationChecker.getSecurityTarget(securityMethod));
    }

    @Test
    public void anonymousAccessAllowed() throws Exception {
        MockHttpServletRequest anonRequest = new MockHttpServletRequest();

        verifyAccessAllowed(AnonymousAllowedEndpoint.class, anonRequest,
                "noAnnotation", "anonymousAllowed");
        verifyAccessAllowed(DenyAllEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyAccessAllowed(NoAnnotationEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyAccessAllowed(DenyAllEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyAccessAllowed(PermitAllEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyAccessAllowed(RolesAllowedAdminEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyAccessAllowed(RolesAllowedUserEndpoint.class, anonRequest,
                "anonymousAllowed");
    }

    @Test
    public void loggedInUserAccessAllowed() throws Exception {
        MockHttpServletRequest loggedInURequest = new MockHttpServletRequest();
        loggedInURequest.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "John Doe";
            }

        });
        verifyAccessAllowed(AnonymousAllowedEndpoint.class, loggedInURequest,
                "noAnnotation", "anonymousAllowed", "permitAll");
        verifyAccessAllowed(DenyAllEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyAccessAllowed(NoAnnotationEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyAccessAllowed(DenyAllEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyAccessAllowed(PermitAllEndpoint.class, loggedInURequest,
                "noAnnotation", "anonymousAllowed", "permitAll");
        verifyAccessAllowed(RolesAllowedAdminEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyAccessAllowed(RolesAllowedUserEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
    }

    @Test
    public void userRoleAccessAllowed() throws Exception {
        MockHttpServletRequest userRoleRequest = new MockHttpServletRequest();
        userRoleRequest.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "John Doe";
            }
        });
        userRoleRequest.addUserRole("user");

        verifyAccessAllowed(AnonymousAllowedEndpoint.class, userRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(NoAnnotationEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(PermitAllEndpoint.class, userRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedAdminEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedUserEndpoint.class, userRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedUserAdmin");
    }

    @Test
    public void userAndAdminRoleAccessAllowed() throws Exception {
        MockHttpServletRequest adminRoleRequest = new MockHttpServletRequest();
        adminRoleRequest.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "John Doe";
            }
        });
        adminRoleRequest.addUserRole("user");
        adminRoleRequest.addUserRole("admin");

        verifyAccessAllowed(AnonymousAllowedEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedAdminEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedUserEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
    }

    @Test
    public void adminRoleAccessAllowed() throws Exception {
        MockHttpServletRequest adminRoleRequest = new MockHttpServletRequest();
        adminRoleRequest.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "John Doe";
            }
        });
        adminRoleRequest.addUserRole("admin");

        verifyAccessAllowed(AnonymousAllowedEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedAdminEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyAccessAllowed(RolesAllowedUserEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
    }

    private void verifyAccessAllowed(Class<?> endpointClass,
            HttpServletRequest request, String... expectedAccessibleMethods)
            throws Exception {
        List<String> expectedAnonList = Arrays
                .asList(expectedAccessibleMethods);
        for (String endpointMethod : ENDPOINT_METHODS) {
            boolean expectedResult = expectedAnonList.contains(endpointMethod);
            Method method = endpointClass.getMethod(endpointMethod);
            Assert.assertEquals("Expected " + endpointClass.getSimpleName()
                    + "." + endpointMethod + " to "
                    + (expectedResult ? "be" : "NOT to be") + " accessible",
                    expectedResult, accessAnnotationChecker
                            .annotationAllowsAccess(method, request));
        }
    }

}
