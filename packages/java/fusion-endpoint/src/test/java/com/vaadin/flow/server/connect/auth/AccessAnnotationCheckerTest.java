package com.vaadin.flow.server.connect.auth;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.connect.AccessControlTestClasses;
import com.vaadin.flow.server.connect.AccessControlTestClasses.AnonymousAllowedEndpoint;
import com.vaadin.flow.server.connect.AccessControlTestClasses.DenyAllEndpoint;
import com.vaadin.flow.server.connect.AccessControlTestClasses.NoAnnotationEndpoint;
import com.vaadin.flow.server.connect.AccessControlTestClasses.PermitAllEndpoint;
import com.vaadin.flow.server.connect.AccessControlTestClasses.RolesAllowedAdminEndpoint;
import com.vaadin.flow.server.connect.AccessControlTestClasses.RolesAllowedUserEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class AccessAnnotationCheckerTest {
    public static final Class<?>[] ENDPOINT_CLASSES = new Class<?>[] {
            AccessControlTestClasses.AnonymousAllowedEndpoint.class,
            AccessControlTestClasses.DenyAllEndpoint.class,
            AccessControlTestClasses.NoAnnotationEndpoint.class,
            AccessControlTestClasses.PermitAllEndpoint.class,
            AccessControlTestClasses.RolesAllowedAdminEndpoint.class,
            AccessControlTestClasses.RolesAllowedUserEndpoint.class };

    public static final String[] ENDPOINT_METHODS = new String[] {
            "noAnnotation", "anonymousAllowed", "permitAll", "denyAll",
            "rolesAllowedUser", "rolesAllowedAdmin", "rolesAllowedUserAdmin" };

    public static final String[] ENDPOINT_NAMES = Stream.of(ENDPOINT_CLASSES)
            .map(cls -> cls.getSimpleName().toLowerCase(Locale.ENGLISH))
            .toArray(String[]::new);

    private static final Principal USER_PRINCIPAL = new Principal() {
        @Override
        public String getName() {
            return "John Doe";
        }
    };

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
        HttpServletRequest anonRequest = createRequest(null);

        verifyMethodAccessAllowed(AnonymousAllowedEndpoint.class, anonRequest,
                "noAnnotation", "anonymousAllowed");
        verifyMethodAccessAllowed(DenyAllEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyMethodAccessAllowed(NoAnnotationEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyMethodAccessAllowed(PermitAllEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyMethodAccessAllowed(RolesAllowedAdminEndpoint.class, anonRequest,
                "anonymousAllowed");
        verifyMethodAccessAllowed(RolesAllowedUserEndpoint.class, anonRequest,
                "anonymousAllowed");
        // Class level access
        verifyClassAccessAllowed(AnonymousAllowedEndpoint.class, anonRequest,
                true);
        verifyClassAccessAllowed(DenyAllEndpoint.class, anonRequest, false);
        verifyClassAccessAllowed(NoAnnotationEndpoint.class, anonRequest,
                false);
        verifyClassAccessAllowed(PermitAllEndpoint.class, anonRequest, false);
        verifyClassAccessAllowed(RolesAllowedAdminEndpoint.class, anonRequest,
                false);
        verifyClassAccessAllowed(RolesAllowedUserEndpoint.class, anonRequest,
                false);
    }

    @Test
    public void loggedInUserAccessAllowed() throws Exception {
        HttpServletRequest loggedInURequest = createRequest(USER_PRINCIPAL);

        verifyMethodAccessAllowed(AnonymousAllowedEndpoint.class,
                loggedInURequest, "noAnnotation", "anonymousAllowed",
                "permitAll");
        verifyMethodAccessAllowed(DenyAllEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyMethodAccessAllowed(NoAnnotationEndpoint.class, loggedInURequest,
                "anonymousAllowed", "permitAll");
        verifyMethodAccessAllowed(PermitAllEndpoint.class, loggedInURequest,
                "noAnnotation", "anonymousAllowed", "permitAll");
        verifyMethodAccessAllowed(RolesAllowedAdminEndpoint.class,
                loggedInURequest, "anonymousAllowed", "permitAll");
        verifyMethodAccessAllowed(RolesAllowedUserEndpoint.class,
                loggedInURequest, "anonymousAllowed", "permitAll");
        // Class level access
        verifyClassAccessAllowed(AnonymousAllowedEndpoint.class,
                loggedInURequest, true);
        verifyClassAccessAllowed(DenyAllEndpoint.class, loggedInURequest,
                false);
        verifyClassAccessAllowed(NoAnnotationEndpoint.class, loggedInURequest,
                false);
        verifyClassAccessAllowed(PermitAllEndpoint.class, loggedInURequest,
                true);
        verifyClassAccessAllowed(RolesAllowedAdminEndpoint.class,
                loggedInURequest, false);
        verifyClassAccessAllowed(RolesAllowedUserEndpoint.class,
                loggedInURequest, false);
    }

    @Test
    public void userRoleAccessAllowed() throws Exception {
        HttpServletRequest userRoleRequest = createRequest(USER_PRINCIPAL,
                "user");

        verifyMethodAccessAllowed(AnonymousAllowedEndpoint.class,
                userRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedUser", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(DenyAllEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(NoAnnotationEndpoint.class, userRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(PermitAllEndpoint.class, userRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedAdminEndpoint.class,
                userRoleRequest, "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedUserEndpoint.class,
                userRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedUser", "rolesAllowedUserAdmin");
        // Class level access
        verifyClassAccessAllowed(AnonymousAllowedEndpoint.class,
                userRoleRequest, true);
        verifyClassAccessAllowed(DenyAllEndpoint.class, userRoleRequest, false);
        verifyClassAccessAllowed(NoAnnotationEndpoint.class, userRoleRequest,
                false);
        verifyClassAccessAllowed(PermitAllEndpoint.class, userRoleRequest,
                true);
        verifyClassAccessAllowed(RolesAllowedAdminEndpoint.class,
                userRoleRequest, false);
        verifyClassAccessAllowed(RolesAllowedUserEndpoint.class,
                userRoleRequest, true);
    }

    @Test
    public void userAndAdminRoleAccessAllowed() throws Exception {
        HttpServletRequest adminRoleRequest = createRequest(USER_PRINCIPAL,
                "user", "admin");

        // Method level access

        verifyMethodAccessAllowed(AnonymousAllowedEndpoint.class,
                adminRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedUser",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedAdminEndpoint.class,
                adminRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedUserEndpoint.class,
                adminRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedUser", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");

        // Class level access
        verifyClassAccessAllowed(AnonymousAllowedEndpoint.class,
                adminRoleRequest, true);
        verifyClassAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                false);
        verifyClassAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                false);
        verifyClassAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                true);
        verifyClassAccessAllowed(RolesAllowedAdminEndpoint.class,
                adminRoleRequest, true);
        verifyClassAccessAllowed(RolesAllowedUserEndpoint.class,
                adminRoleRequest, true);
    }

    @Test
    public void adminRoleAccessAllowed() throws Exception {
        HttpServletRequest adminRoleRequest = createRequest(USER_PRINCIPAL,
                "admin");

        verifyMethodAccessAllowed(AnonymousAllowedEndpoint.class,
                adminRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                "anonymousAllowed", "permitAll", "rolesAllowedAdmin",
                "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                "noAnnotation", "anonymousAllowed", "permitAll",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedAdminEndpoint.class,
                adminRoleRequest, "noAnnotation", "anonymousAllowed",
                "permitAll", "rolesAllowedAdmin", "rolesAllowedUserAdmin");
        verifyMethodAccessAllowed(RolesAllowedUserEndpoint.class,
                adminRoleRequest, "anonymousAllowed", "permitAll",
                "rolesAllowedAdmin", "rolesAllowedUserAdmin");

        // Class level access
        verifyClassAccessAllowed(AnonymousAllowedEndpoint.class,
                adminRoleRequest, true);
        verifyClassAccessAllowed(DenyAllEndpoint.class, adminRoleRequest,
                false);
        verifyClassAccessAllowed(NoAnnotationEndpoint.class, adminRoleRequest,
                false);
        verifyClassAccessAllowed(PermitAllEndpoint.class, adminRoleRequest,
                true);
        verifyClassAccessAllowed(RolesAllowedAdminEndpoint.class,
                adminRoleRequest, true);
        verifyClassAccessAllowed(RolesAllowedUserEndpoint.class,
                adminRoleRequest, false);
    }

    private HttpServletRequest createRequest(Principal userPrincipal,
            String... roles) {
        Set<String> roleSet = new HashSet<>();
        Collections.addAll(roleSet, roles);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getUserPrincipal()).thenReturn(userPrincipal);
        Mockito.when(request.isUserInRole(Mockito.anyString()))
                .thenAnswer(query -> {
                    return roleSet.contains(query.getArguments()[0]);
                });
        return request;
    }

    private void verifyMethodAccessAllowed(Class<?> endpointClass,
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

    private void verifyClassAccessAllowed(Class<?> cls,
            HttpServletRequest request, boolean expectedResult)
            throws Exception {
        Assert.assertEquals(
                "Expected " + cls.getSimpleName() + " to "
                        + (expectedResult ? "be" : "NOT to be") + " accessible",
                expectedResult,
                accessAnnotationChecker.annotationAllowsAccess(cls, request));
    }

}
