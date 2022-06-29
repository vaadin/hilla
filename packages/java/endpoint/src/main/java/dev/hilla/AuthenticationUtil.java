package dev.hilla;

import java.util.function.Function;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication;

    }

    /**
     * Gets a function for checking if the authenticated user from the Spring
     * SecurityContextHolder is in a given role.
     *
     * @return a function for checking if the given user has the given role
     */
    public static Function<String, Boolean> getSecurityHolderRoleChecker() {
        Authentication authentication = getSecurityHolderAuthentication();
        if (authentication == null) {
            return role -> false;
        }
        return role -> authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals("ROLE_" + role));
    }

}
