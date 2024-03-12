package com.vaadin.hilla.route;

import com.vaadin.hilla.route.records.ClientViewConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Optional;
import org.springframework.http.server.RequestPath;

/**
 * A container for utility methods related with Routes.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Component
public class RouteUtil {

    private final ClientRouteRegistry registry;

    /**
     * Initializes a new instance of the RouteUtil class with the given
     * registry.
     *
     * @param registry
     *            - the registry to use
     */
    @Autowired
    public RouteUtil(final ClientRouteRegistry registry) {
        this.registry = registry;
    }

    /**
     * Checks if the given request is allowed route to the user.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes allowed route,
     *         <code>false</code> otherwise
     */
    public boolean isRouteAllowed(HttpServletRequest request) {
        var viewConfig = getRouteData(request);
        if (viewConfig.isEmpty()) {
            return false;
        }
        if (viewConfig.get().requiresLogin()
                && request.getUserPrincipal() == null) {
            return false;
        }
        // current roles logic means that an empty array denies access to all
        var rolesAllowed = viewConfig.get().rolesAllowed();
        if (rolesAllowed != null) {
            for (var role : rolesAllowed) {
                if (request.isUserInRole(role)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private Optional<ClientViewConfig> getRouteData(
            HttpServletRequest request) {
        var requestPath = RequestPath.parse(request.getRequestURI(),
                request.getContextPath());
        return Optional.ofNullable(registry
                .getRouteByPath(requestPath.pathWithinApplication().value()));
    }
}
