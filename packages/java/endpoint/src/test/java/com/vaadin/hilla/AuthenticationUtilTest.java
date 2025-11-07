package com.vaadin.hilla;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.security.VaadinRolePrefixHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationUtilTest {

    private SecurityContext securityContext;

    @Before
    public void setUp() {
        securityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
        CurrentInstance.clearAll();
    }

    @Test
    public void getSecurityHolderAuthentication_noAuthentication_returnsNull() {
        assertNull(AuthenticationUtil.getSecurityHolderAuthentication());
    }

    @Test
    public void getSecurityHolderAuthentication_anonymousAuthentication_returnsNull() {
        var anonymousAuth = new AnonymousAuthenticationToken("key", "anonymous",
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        securityContext.setAuthentication(anonymousAuth);

        assertNull(AuthenticationUtil.getSecurityHolderAuthentication());
    }

    @Test
    public void getSecurityHolderAuthentication_validAuthentication_returnsAuthentication() {
        var auth = createAuthentication("user", "ROLE_USER");
        securityContext.setAuthentication(auth);

        var result = AuthenticationUtil.getSecurityHolderAuthentication();
        assertNotNull(result);
        assertTrue(result instanceof UsernamePasswordAuthenticationToken);
    }

    @Test
    public void getSecurityHolderRoleChecker_noAuthentication_returnsFalse() {
        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker();

        assertFalse(roleChecker.apply("USER"));
        assertFalse(roleChecker.apply("ADMIN"));
    }

    @Test
    public void getSecurityHolderRoleChecker_withDefaultPrefix_checksRoleCorrectly() {
        var auth = createAuthentication("user", "ROLE_USER", "ROLE_ADMIN");
        securityContext.setAuthentication(auth);

        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker();

        assertTrue(roleChecker.apply("USER"));
        assertTrue(roleChecker.apply("ADMIN"));
        assertFalse(roleChecker.apply("GUEST"));
    }

    @Test
    public void getSecurityHolderRoleChecker_withCustomPrefix_checksRoleCorrectly() {
        var auth = createAuthentication("user", "CUSTOM_USER", "CUSTOM_ADMIN");
        securityContext.setAuthentication(auth);

        var roleChecker = AuthenticationUtil
                .getSecurityHolderRoleChecker("CUSTOM_");

        assertTrue(roleChecker.apply("USER"));
        assertTrue(roleChecker.apply("ADMIN"));
        assertFalse(roleChecker.apply("GUEST"));
    }

    @Test
    public void getSecurityHolderRoleChecker_withNullPrefix_checksRoleCorrectly() {
        var auth = createAuthentication("user", "USER", "ADMIN");
        securityContext.setAuthentication(auth);

        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker(null);

        assertTrue(roleChecker.apply("USER"));
        assertTrue(roleChecker.apply("ADMIN"));
        assertFalse(roleChecker.apply("GUEST"));
    }

    @Test
    public void getSecurityHolderRoleChecker_roleAlreadyHasPrefix_doesNotAddPrefix() {
        var auth = createAuthentication("user", "ROLE_USER");
        securityContext.setAuthentication(auth);

        var roleChecker = AuthenticationUtil
                .getSecurityHolderRoleChecker("ROLE_");

        assertTrue(roleChecker.apply("ROLE_USER"));
        assertTrue(roleChecker.apply("USER"));
    }

    @Test
    public void getSecurityHolderRoleChecker_usesVaadinRolePrefixHolder_whenAvailable() {
        var auth = createAuthentication("user", "APP_USER", "APP_ADMIN");
        securityContext.setAuthentication(auth);

        // Setup VaadinService with VaadinRolePrefixHolder
        var service = mock(VaadinService.class);
        var context = mock(VaadinContext.class);
        var lookup = mock(Lookup.class);
        var rolePrefixHolder = new VaadinRolePrefixHolder("APP_");

        when(service.getContext()).thenReturn(context);
        when(context.getAttribute(Lookup.class)).thenReturn(lookup);
        when(lookup.lookup(VaadinRolePrefixHolder.class))
                .thenReturn(rolePrefixHolder);

        CurrentInstance.set(VaadinService.class, service);

        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker();

        assertTrue(roleChecker.apply("USER"));
        assertTrue(roleChecker.apply("ADMIN"));
        assertFalse(roleChecker.apply("GUEST"));
    }

    @Test
    public void getSecurityHolderRoleChecker_fallsBackToDefaultPrefix_whenVaadinRolePrefixHolderNotAvailable() {
        var auth = createAuthentication("user", "ROLE_USER");
        securityContext.setAuthentication(auth);

        // No VaadinService setup, should use default "ROLE_" prefix
        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker();

        assertTrue(roleChecker.apply("USER"));
        assertFalse(roleChecker.apply("ADMIN"));
    }

    @Test
    public void getSecurityHolderRoleChecker_withEmptyPrefix_checksRoleCorrectly() {
        var auth = createAuthentication("user", "USER", "ADMIN");
        securityContext.setAuthentication(auth);

        var roleChecker = AuthenticationUtil.getSecurityHolderRoleChecker("");

        assertTrue(roleChecker.apply("USER"));
        assertTrue(roleChecker.apply("ADMIN"));
        assertFalse(roleChecker.apply("GUEST"));
    }

    private Authentication createAuthentication(String username,
            String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(username, "password",
                authorities);
    }
}
