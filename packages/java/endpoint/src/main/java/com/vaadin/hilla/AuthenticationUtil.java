package com.vaadin.hilla;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.security.VaadinRolePrefixHolder;

/**
 * Helpers for authentication related tasks.
 */
public class AuthenticationUtil {

    /**
     * Gets the authenticated user from the Spring SecurityContextHolder.
     *
     * @return the authenticated user or {@code null}
     */
    public static Authentication getSecurityHolderAuthentication() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication;

    }

    /**
     * Gets a function for checking if the authenticated user from the Spring
     * SecurityContextHolder is in a given role. Given role is prefixed with the
     * role prefix from {@link VaadinRolePrefixHolder} if available, otherwise
     * defaults to 'ROLE_'.
     *
     * @return a function for checking if the given user has the given role
     */
    public static Function<String, Boolean> getSecurityHolderRoleChecker() {
        var rolePrefix = Optional.ofNullable(VaadinService.getCurrent())
                .map(service -> service.getContext().getAttribute(Lookup.class))
                .map(lookup -> lookup.lookup(VaadinRolePrefixHolder.class))
                .map(VaadinRolePrefixHolder::getRolePrefix).orElse("ROLE_");
        return getSecurityHolderRoleChecker(rolePrefix);
    }

    /**
     * Gets a function for checking if the authenticated user from the Spring
     * SecurityContextHolder is in a given role.
     *
     * @param rolePrefix
     *            Prefix for the given role.
     * @return a function for checking if the given user has the given role
     */
    public static Function<String, Boolean> getSecurityHolderRoleChecker(
            String rolePrefix) {
        var authentication = getSecurityHolderAuthentication();
        if (authentication == null) {
            return role -> false;
        }

        return role -> {
            var roleWithPrefix = (rolePrefix != null && role != null
                    && !role.startsWith(rolePrefix)) ? rolePrefix + role : role;
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority
                            .getAuthority().equals(roleWithPrefix));
        };
    }

}
