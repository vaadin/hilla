package com.vaadin.hilla.route.records;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of TypeScript's Hilla ConfigView.
 * Represents a view configuration
 * from Hilla file-system-routing module.
 * @see <a href="https://github.com/vaadin/hilla/tree/main/packages/ts/hilla-file-router/src/utils.ts#L3">ConfigView</a>
 *
 * @param other - a map of unknown values
 */
public record ClientViewConfig(String title, String[] rolesAllowed, Boolean requiresLogin, String route, Boolean lazy,
                               Boolean register, ClientViewMenuConfig menu,
                               @JsonProperty("params") Map<String, RouteParamType> routeParameters, Map<String, Object> other) {
    /**
     * Default constructor
     * with initialization of unknown values.
     */
    public ClientViewConfig {
        if (other == null) {
            other = new HashMap<>();
        }
    }

    /**
     * Add a key-value pair for all unknown fields.
     * @param key - the key
     * @param value - the value
     */
    @JsonAnySetter
    public void add(String key, Object value) {
        other.put(key, value);
    }

    /**
     * Get all unknown values.
     * @return a map of unknown values
     */
    @JsonAnyGetter
    public Map<String, Object> getOther() {
        return other;
    }

    @Override
    public String toString() {
        return "ClientViewConfig{" +
            "title='" + title + '\'' +
            ", rolesAllowed=" + Arrays.toString(rolesAllowed) +
            ", requiresLogin=" + requiresLogin +
            ", route='" + route + '\'' +
            ", lazy=" + lazy +
            ", register=" + register +
            ", menu=" + menu +
            ", other=" + other +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientViewConfig that = (ClientViewConfig) o;
        return Objects.equals(title, that.title)
            && Arrays.equals(rolesAllowed, that.rolesAllowed)
            && Objects.equals(requiresLogin, that.requiresLogin)
            && Objects.equals(route, that.route)
            && Objects.equals(lazy, that.lazy)
            && Objects.equals(register, that.register)
            && Objects.equals(menu, that.menu)
            && Objects.equals(other, that.other);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(title, route, lazy, register, menu, other);
        result = 31 * result + Arrays.hashCode(rolesAllowed);
        return result;
    }
}
