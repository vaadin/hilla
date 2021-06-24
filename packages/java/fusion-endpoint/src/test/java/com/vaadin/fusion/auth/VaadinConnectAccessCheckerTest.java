package com.vaadin.fusion.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.security.Principal;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("unused")
public class VaadinConnectAccessCheckerTest {
    private static final String ROLE_USER = "ROLE_USER";

    private VaadinConnectAccessChecker checker;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @Before
    public void before() {
        checker = new VaadinConnectAccessChecker(new AccessAnnotationChecker(),
                new CsrfChecker());
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(sessionMock
                .getAttribute(VaadinService.getCsrfTokenAttributeName()))
                        .thenReturn("Vaadin CCDM");
        when(requestMock.getSession(false)).thenReturn(sessionMock);
        when(requestMock.getUserPrincipal()).thenReturn(mock(Principal.class));
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn("Vaadin CCDM");
        when(requestMock.isUserInRole("ROLE_USER")).thenReturn(true);
    }

    private void createAnonymousContext() {
        when(requestMock.getUserPrincipal()).thenReturn(null);
    }

    private void createDifferentSessionToken() {
        when(sessionMock
                .getAttribute(VaadinService.getCsrfTokenAttributeName()))
                        .thenReturn("CCDM Token");
    }

    private void createNullTokenContextInHeaderRequest() {
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn(null);
    }

    private void createNullTokenSession() {
        when(sessionMock
                .getAttribute(VaadinService.getCsrfTokenAttributeName()))
                        .thenReturn(null);
    }

    private void createNullSession() {
        when(requestMock.getSession(false)).thenReturn(null);
        when(requestMock.getSession()).thenReturn(null);
    }

    private void shouldPass(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNull(checker.check(method, requestMock));
    }

    private void shouldFail(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNotNull(checker.check(method, requestMock));
    }

    @Test
    public void should_fail_When_not_having_token_in_headerRequest()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        createNullTokenContextInHeaderRequest();
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_not_having_token_in_session_but_have_token_in_request_header()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        createNullTokenSession();
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_not_having_token_in_session_but_have_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {
            }
        }
        createNullTokenSession();
        shouldFail(Test.class);
    }

    @Test
    public void should_pass_When_not_having_session_And_not_having_token_in_request_header()
            throws Exception {
        @PermitAll
        class Test {
            public void test() {
            }
        }
        createNullSession();
        createNullTokenContextInHeaderRequest();
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_not_having_session_And_not_having_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {
            }
        }
        createNullSession();
        createNullTokenContextInHeaderRequest();
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_csrf_disabled() throws Exception {
        class Test {
            @PermitAll
            public void test() {
            }
        }
        createNullTokenContextInHeaderRequest();
        checker.enableCsrf(false);
        shouldPass(Test.class);
    }

    @Test
    public void should_fail_When_having_different_token_between_session_and_headerRequest()
            throws Exception {
        class Test {
            public void test() {
            }
        }
        createDifferentSessionToken();
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_having_different_token_between_session_and_headerRequest_and_NoAuthentication_AnonymousAllowed()
            throws Exception {
        class Test {
            @AnonymousAllowed
            public void test() {
            }
        }
        createAnonymousContext();
        createDifferentSessionToken();
        shouldFail(Test.class);
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
            assertEquals(VaadinConnectAccessChecker.ACCESS_DENIED_MSG_DEV_MODE,
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
            assertEquals(VaadinConnectAccessChecker.ACCESS_DENIED_MSG,
                    accessDeniedMessage);
        } finally {
            CurrentInstance.clearAll();
        }
    }
}
