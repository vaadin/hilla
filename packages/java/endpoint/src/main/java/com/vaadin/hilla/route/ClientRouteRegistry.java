/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.route;

import com.vaadin.hilla.route.records.ClientViewConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of registered client side routes.
 */
@Component
public class ClientRouteRegistry implements Serializable {

    /**
     * A map of registered routes and their corresponding client view
     * configurations with ordered insertion.
     */
    private final Map<String, ClientViewConfig> registeredRoutes = new LinkedHashMap<>();

    /**
     * Returns all registered routes.
     *
     * @return a map of all registered routes
     */
    public Map<String, ClientViewConfig> getAllRoutes() {
        return Map.copyOf(registeredRoutes);
    }

    /**
     * Clears all registered routes.
     */
    public void clearRoutes() {
        registeredRoutes.clear();
    }

    /**
     * Adds a new route to the registry.
     *
     * @param route
     *            the route to add
     * @param clientView
     *            the client view to add
     */
    public void addRoute(String route, ClientViewConfig clientView) {
        registeredRoutes.put(route, clientView);
    }

    /**
     * Removes a route from the registry.
     *
     * @param route
     *            the route to remove
     */
    public void removeRoute(String route) {
        registeredRoutes.remove(route);
    }

    /**
     * Gets the client view configuration for the given route.
     *
     * @param path
     *            the URL path to get the client view configuration for
     * @return - the client view configuration for the given route
     */
    public ClientViewConfig getRouteByPath(String path) {
        final Set<String> routes = registeredRoutes.keySet();
        final AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String route : routes) {
            if (pathMatcher.match(route, path)) {
                return registeredRoutes.get(route);
            }
        }
        return null;
    }
}
