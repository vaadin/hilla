package com.vaadin.hilla.route;

import com.vaadin.hilla.route.records.ClientViewConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * A container for utility methods related with Routes.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Component
// TODO: move implenets to Flow
// com.vaadin.flow.internal.hilla.RouteRequestUtil
public class RouteUtil implements RouteRequestUtil {

    private final ClientRouteRegistry registry;

    /**
     * Initializes a new instance of the RouteUtil class with
     * the given registry.
     *
     * @param registry      - the registry to use
     */
    @Autowired
    public RouteUtil(final ClientRouteRegistry registry) {
        this.registry = registry;
    }

    /**
     * Checks if the request is for a client side route.
     * <p>
     * Note even if this method returns <code>true</code>, there is no guarantee
     * that an endpoint method will actually be called, e.g. access might be
     * denied.
     *
     * @param request the HTTP request
     * @return <code>true</code> if the request is a client side route,
     * <code>false</code> otherwise
     */
    @Override
    public boolean isRouteRequest(HttpServletRequest request) {
        return getRouteData(request).isPresent();
    }

    /**
     * Checks if the given request goes to an anonymous (public) routes.
     *
     * @param request the HTTP request to check
     * @return <code>true</code> if the request goes to an anonymous routes,
     * <code>false</code> otherwise
     */
    @Override
    public boolean isAnonymousRoute(HttpServletRequest request) {
        var viewConfig = getRouteData(request);
        if (viewConfig.isEmpty()) {
            return false;
        }
        final String[] rolesAllowed = viewConfig.get().rolesAllowed();

        return isAnonymousAllowed(rolesAllowed);
    }

    /**
     * Checks if the given request goes to an authorized route.
     * (user needs specific role for access)
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes to an authorized route,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isAuthenticatedRoute(HttpServletRequest request) {
        var viewConfig = getRouteData(request);
        if (viewConfig.isEmpty()) {
            return false;
        }
        final String[] rolesAllowed = viewConfig.get().rolesAllowed();

        return !isAnonymousAllowed(rolesAllowed);
    }

    /**
     * Checks if the given request is allowed route to the user.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes allowed route,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isRouteAllowed(HttpServletRequest request) {
        var viewConfig = getRouteData(request);
        if (viewConfig.isEmpty()) {
            return false;
        }
        final String[] rolesAllowed = viewConfig.get().rolesAllowed();
        if (isAnonymousAllowed(rolesAllowed)) {
            return true;
        } else {
            for (String role : rolesAllowed) {
                if (request.isUserInRole(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAnonymousAllowed(final String[] rolesAllowed) {
        return rolesAllowed == null || rolesAllowed.length == 0
            || Arrays.stream(rolesAllowed)
            .anyMatch(role -> role.equalsIgnoreCase("anonymous"));
    }

    private Optional<ClientViewConfig> getRouteData(HttpServletRequest request) {
        var requestPath = RequestPath.parse(request.getRequestURI(), request.getContextPath());
        return Optional.ofNullable(registry.getRouteByPath(requestPath.pathWithinApplication().value()));
    }
}
