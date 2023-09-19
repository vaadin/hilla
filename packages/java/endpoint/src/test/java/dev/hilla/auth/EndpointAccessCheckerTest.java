package dev.hilla.auth;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.ApplicationConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
public class EndpointAccessCheckerTest {
    private static final String ROLE_USER = "ROLE_USER";

    private EndpointAccessChecker checker;
    private HttpServletRequest requestMock;

    @Before
    public void before() {
        checker = new EndpointAccessChecker(new AccessAnnotationChecker());
        requestMock = mock(HttpServletRequest.class);
        when(requestMock.getUserPrincipal()).thenReturn(mock(Principal.class));
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn("Vaadin Fusion");
        when(requestMock.getCookies()).thenReturn(new Cookie[] {
                new Cookie(ApplicationConstants.CSRF_TOKEN, "Vaadin Fusion") });
        when(requestMock.isUserInRole("ROLE_USER")).thenReturn(true);
    }

    private void createAnonymousContext() {
        when(requestMock.getUserPrincipal()).thenReturn(null);
    }

    private void shouldPass(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNull(checker.check(method, requestMock));
    }

    private void shouldPassInherited(Class<?> test) throws Exception {
        Method method = test.getMethod("sayHello");
        if (method.getDeclaringClass().equals(test)) {
            assertNull(checker.check(method, requestMock));
        } else {
            assertNull(checker.check(test, requestMock));
        }
    }

    private void shouldFail(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNotNull(checker.check(method, requestMock));
    }

    private void shouldFailInherited(Class<?> test) throws Exception {
        Method method = test.getMethod("sayHello");
        if (method.getDeclaringClass().equals(test)) {
            assertNotNull(checker.check(method, requestMock));
        } else {
            assertNotNull(checker.check(test, requestMock));
        }
    }

    @Test
    public void should_Fail_When_NoAuthentication() throws Exception {
        class Test {
            public void test() {
            }
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_Fail_When_Authentication_And_matching_token()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        shouldFail(Test.class);
    }

    @Test
    public void should_Pass_When_PermitAll() throws Exception {
        @PermitAll
        class Test {
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_Fail_When_DenyAllClass() throws Exception {
        @DenyAll
        class Test {
            public void test() {
            }
        }
        shouldFail(Test.class);
    }

    @Test()
    public void should_Pass_When_DenyAllClass_ValidRoleMethod()
            throws Exception {
        @DenyAll
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_DenyAllClass_PermitAllMethod()
            throws Exception {
        @DenyAll
        class Test {
            @PermitAll
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Fail_When_InvalidRoleClass() throws Exception {
        @RolesAllowed({ "ROLE_ADMIN" })
        class Test {
            public void test() {
            }
        }
        shouldFail(Test.class);
    }

    @Test()
    public void should_Pass_When_InvalidRoleClass_ValidRoleMethod()
            throws Exception {
        @RolesAllowed({ "ROLE_ADMIN" })
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_InvalidRoleClass_PermitAllMethod()
            throws Exception {
        @RolesAllowed({ "ROLE_ADMIN" })
        class Test {
            @PermitAll
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_ValidRoleClass() throws Exception {
        @RolesAllowed(ROLE_USER)
        class Test {
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_ClassIsAnnotated()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {
            }
        }

        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_MethodIsAnnotated()
            throws Exception {
        class Test {
            @AnonymousAllowed
            public void test() {
            }
        }
        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_NotAllowAnonymousAccess_When_NoAnnotationsPresent()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_AllowAnyAuthenticatedAccess_When_PermitAllAndAnonymousAllowed()
            throws Exception {
        class Test {
            @PermitAll
            @AnonymousAllowed
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_PermitAllAndAnonymousAllowed()
            throws Exception {
        class Test {
            @PermitAll
            @AnonymousAllowed
            public void test() {
            }
        }

        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnyAuthenticatedAccess_When_RolesAllowedAndAnonymousAllowed()
            throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @AnonymousAllowed
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_RolesAllowedAndAnonymousAllowed()
            throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @AnonymousAllowed
            public void test() {
            }
        }
        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_DisallowAnyAuthenticatedAccess_When_DenyAllAndAnonymousAllowed()
            throws Exception {
        class Test {
            @DenyAll
            @AnonymousAllowed
            public void test() {
            }
        }
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowNotMatchingRoleAccess_When_RolesAllowedAndPermitAll()
            throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @PermitAll
            public void test() {
            }
        }
        shouldFail(Test.class);
    }

    @Test
    public void should_AllowSpecificRoleAccess_When_RolesAllowedAndPermitAll()
            throws Exception {
        class Test {
            @RolesAllowed(ROLE_USER)
            @PermitAll
            public void test() {
            }
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_DenyAllAndAnonymousAllowed()
            throws Exception {
        class Test {
            @DenyAll
            @AnonymousAllowed
            public void test() {
            }
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithDenyAll()
            throws Exception {
        @AnonymousAllowed
        class Test {
            @DenyAll
            public void test() {
            }
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithRolesAllowed()
            throws Exception {
        @AnonymousAllowed
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {
            }
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithPermitAll()
            throws Exception {
        @AnonymousAllowed
        class Test {
            @PermitAll
            public void test() {
            }
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_showHelpfulMessage_When_accessDeniedInDevMode()
            throws Exception {
        VaadinService mockService = Mockito.mock(VaadinService.class);
        DeploymentConfiguration mockDeploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(mockService.getDeploymentConfiguration())
                .thenReturn(mockDeploymentConfiguration);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
                .thenReturn(false);
        CurrentInstance.set(VaadinService.class, mockService);
        try {
            class Test {
                public void test() {
                }
            }
            Method method = Test.class.getMethod("test");
            String accessDeniedMessage = checker.check(method, requestMock);
            assertEquals(EndpointAccessChecker.ACCESS_DENIED_MSG_DEV_MODE,
                    accessDeniedMessage);
            assertTrue(accessDeniedMessage
                    .contains(PermitAll.class.getSimpleName()));
            assertTrue(accessDeniedMessage
                    .contains(RolesAllowed.class.getSimpleName()));
            assertTrue(accessDeniedMessage
                    .contains(AnonymousAllowed.class.getSimpleName()));
        } finally {
            CurrentInstance.clearAll();
        }
    }

    @Test
    public void should_notShowHelpfulMessage_When_accessDeniedInProductionMode()
            throws Exception {
        VaadinService mockService = Mockito.mock(VaadinService.class);
        DeploymentConfiguration mockDeploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(mockService.getDeploymentConfiguration())
                .thenReturn(mockDeploymentConfiguration);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
                .thenReturn(true);
        CurrentInstance.set(VaadinService.class, mockService);
        try {
            class Test {
                public void test() {
                }
            }
            Method method = Test.class.getMethod("test");
            String accessDeniedMessage = checker.check(method, requestMock);
            assertEquals(EndpointAccessChecker.ACCESS_DENIED_MSG,
                    accessDeniedMessage);
        } finally {
            CurrentInstance.clearAll();
        }
    }

    @Test
    public void should_fail_When_Endpoint_is_not_annotated() throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldFailInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_overridden_method_is_AnonymousAllowed()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        class Test extends ParentEndpoint {

            @Override
            @AnonymousAllowed
            public String sayHello() {
                return "Hello from Test Endpoint";
            }

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_Endpoint_is_DenyAll() throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @DenyAll
        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldFailInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_is_AnonymousAllowed()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @AnonymousAllowed
        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_is_PermitAll() throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @PermitAll
        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_method_is_PermitAll()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        class Test extends ParentEndpoint {

            @Override
            @PermitAll
            public String sayHello() {
                return "Hello from Test Endpoint";
            }

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_is_RolesAllowed_User()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @RolesAllowed(ROLE_USER)
        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_Endpoint_method_is_RolesAllowed_User()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        class Test extends ParentEndpoint {

            @Override
            @RolesAllowed(ROLE_USER)
            public String sayHello() {
                return "Hello from Test Endpoint";
            }

            public void test() {
            }
        }

        shouldPassInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_Endpoint_is_RolesAllowed_Admin()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @RolesAllowed("ROLE_ADMIN")
        class Test extends ParentEndpoint {

            public void test() {
            }
        }

        shouldFailInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_Endpoint_method_is_RolesAllowed_Admin()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        class Test extends ParentEndpoint {

            @Override
            @RolesAllowed("ROLE_ADMIN")
            public String sayHello() {
                return "Hello from Test Endpoint";
            }

            public void test() {
            }
        }

        shouldFailInherited(Test.class);
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_Endpoint_method_is_RolesAllowed_Admin_Endpoint_PermitAll()
            throws Exception {

        class ParentEndpoint {
            public String sayHello() {
                return "Hello from ParentEndpoint";
            }
        }

        @PermitAll
        class Test extends ParentEndpoint {

            @RolesAllowed("ROLE_ADMIN")
            @Override
            public String sayHello() {
                return "Hello from Test Endpoint";
            }

            public void test() {
            }
        }

        shouldFailInherited(Test.class);
        shouldPass(Test.class);
    }

}
