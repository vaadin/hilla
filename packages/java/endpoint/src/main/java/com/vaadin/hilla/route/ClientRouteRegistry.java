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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.hilla.route.records.ClientViewConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import com.vaadin.flow.router.internal.ClientRoutesProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Keeps track of registered client side routes.
 */
@Component
public class ClientRouteRegistry implements ClientRoutesProvider {

    public static final String FILE_ROUTES_JSON_NAME = "file-routes.json";
    public static final String FILE_ROUTES_JSON_PROD_PATH = "/META-INF/VAADIN/"
            + FILE_ROUTES_JSON_NAME;

    /**
     * A map of registered routes and their corresponding client view
     * configurations with ordered insertion.
     */
    private final Map<String, ClientViewConfig> registeredRoutes = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientRouteRegistry.class);

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
        return Stream.of(addTrailingSlash(path), removeTrailingSlash(path))
                .map(p -> {
                    for (String route : routes) {
                        if (pathMatcher.match(route, p)) {
                            return registeredRoutes.get(route);
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private String addTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + '/';
    }

    private String removeTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    /**
     * Registers client routes from file-routes.json file generated by the
     * file-router's Vite plugin. The file-routes.json file is expected to be in
     * the frontend/generated folder in dev mode and in the META-INF/VAADIN
     * folder in production mode.
     *
     * @param deploymentConfiguration
     *            the deployment configuration
     *
     * @return {@code true} if the client routes were successfully registered,
     *         {@code false} otherwise
     */
    public boolean registerClientRoutes(
            DeploymentConfiguration deploymentConfiguration) {
        var viewsJsonAsResource = getViewsJsonAsResource(
                deploymentConfiguration);
        if (viewsJsonAsResource == null) {
            LOGGER.debug(
                    "No {} found under {} directory. Skipping client route registration.",
                    FILE_ROUTES_JSON_NAME,
                    deploymentConfiguration.isProductionMode()
                            ? "'META-INF/VAADIN'"
                            : "'frontend/generated'");
            return false;
        }
        try (var source = viewsJsonAsResource.openStream()) {
            if (source != null) {
                clearRoutes();
                registerAndRecurseChildren("",
                        mapper.readValue(source, new TypeReference<>() {
                        }));
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.warn("Failed load {} from {}", FILE_ROUTES_JSON_NAME,
                    viewsJsonAsResource.getPath(), e);
            return false;
        }
    }

    private URL getViewsJsonAsResource(
            DeploymentConfiguration deploymentConfiguration) {
        var isProductionMode = deploymentConfiguration.isProductionMode();
        if (isProductionMode) {
            return getClass().getResource(FILE_ROUTES_JSON_PROD_PATH);
        }
        try {
            return deploymentConfiguration.getFrontendFolder().toPath()
                    .resolve("generated").resolve(FILE_ROUTES_JSON_NAME).toUri()
                    .toURL();
        } catch (MalformedURLException e) {
            LOGGER.warn("Failed to find {} under frontend/generated",
                    FILE_ROUTES_JSON_NAME, e);
            throw new RuntimeException(e);
        }
    }

    private void registerAndRecurseChildren(String basePath,
            ClientViewConfig view) {
        var path = view.getRoute() == null || view.getRoute().isEmpty()
                ? basePath
                : basePath + '/' + view.getRoute();
        if (view.getChildren() == null || view.getChildren().isEmpty()) {
            addRoute(path, view);
        } else {
            view.getChildren().forEach(child -> {
                child.setParent(view);
                registerAndRecurseChildren(path, child);
            });
        }
    }

    @Override
    public List<String> getClientRoutes() {
        return getAllRoutes().keySet().stream().toList();
    }
}
