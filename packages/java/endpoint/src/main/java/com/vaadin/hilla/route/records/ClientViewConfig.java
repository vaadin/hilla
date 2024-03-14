package com.vaadin.hilla.route.records;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of TypeScript's Hilla ConfigView. Represents a view
 * configuration from Hilla file-system-routing module.
 *
 * @see <a href=
 *      "https://github.com/vaadin/hilla/tree/main/packages/ts/hilla-file-router/src/utils.ts#L3">ConfigView</a>
 */
public final class ClientViewConfig {
    private final String path;
    private final String title;
    private final String[] rolesAllowed;
    private final Boolean requiresLogin;
    private final String route;
    private final Boolean lazy;
    private final Boolean register;
    private final ClientViewMenuConfig menu;
    private final List<ClientViewConfig> children;
    @JsonProperty("params")
    private final Map<String, RouteParamType> routeParameters;
    private final Map<String, Object> other;
    private ClientViewConfig parent;

    /**
     * Default constructor with initialization of unknown values.
     */
    public ClientViewConfig(String path, String title, String[] rolesAllowed,
            Boolean requiresLogin, String route, Boolean lazy, Boolean register,
            ClientViewMenuConfig menu, List<ClientViewConfig> children,
            @JsonProperty("params") Map<String, RouteParamType> routeParameters,
            Map<String, Object> other) {
        if (other == null) {
            other = new HashMap<>();
        }
        this.path = path;
        this.title = title;
        this.rolesAllowed = rolesAllowed;
        this.requiresLogin = requiresLogin;
        this.route = route;
        this.lazy = lazy;
        this.register = register;
        this.menu = menu;
        this.children = children;
        this.routeParameters = routeParameters;
        this.other = other;
    }

    /**
     * Add a key-value pair for all unknown fields.
     *
     * @param key
     *            - the key
     * @param value
     *            - the value
     */
    @JsonAnySetter
    public void add(String key, Object value) {
        other.put(key, value);
    }

    /**
     * Get all unknown values.
     *
     * @return a map of unknown values
     */
    @JsonAnyGetter
    public Map<String, Object> getOther() {
        return other;
    }

    public String path() {
        return path;
    }

    public String title() {
        return title;
    }

    public String[] rolesAllowed() {
        return rolesAllowed;
    }

    public Boolean requiresLogin() {
        return requiresLogin;
    }

    public String route() {
        return route;
    }

    public Boolean lazy() {
        return lazy;
    }

    public Boolean register() {
        return register;
    }

    public ClientViewMenuConfig menu() {
        return menu;
    }

    public List<ClientViewConfig> children() {
        return children;
    }

    @JsonProperty("params")
    public Map<String, RouteParamType> routeParameters() {
        return routeParameters;
    }

    public Map<String, Object> other() {
        return other;
    }

    public ClientViewConfig getParent() {
        return parent;
    }

    public void setParent(ClientViewConfig parent) {
        this.parent = parent;
    }
}
