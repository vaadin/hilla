package com.vaadin.hilla.route.records;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a server side view
 * configuration for the client side.
 * @param route
 * @param title
 * @param rolesAllowed
 * @param lazy
 *  @param register
 *  @param menu
 *  @param routeParameters
 */
public record AvailableViewInfo(String title, String[] rolesAllowed, String route, Boolean lazy, Boolean register,
                                ClientViewMenuConfig menu, @JsonProperty("params") Map<String, RouteParamType> routeParameters) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AvailableViewInfo that = (AvailableViewInfo) o;
        return Objects.equals(title, that.title) && Arrays.equals(rolesAllowed, that.rolesAllowed) && Objects.equals(route, that.route) && Objects.equals(lazy, that.lazy) && Objects.equals(register, that.register) && Objects.equals(menu, that.menu) && Objects.equals(routeParameters, that.routeParameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(title, route, lazy, register, menu, routeParameters);
        result = 31 * result + Arrays.hashCode(rolesAllowed);
        return result;
    }

    @Override
    public String toString() {
        return "AvailableViewInfo{" +
            "title='" + title + '\'' +
            ", rolesAllowed=" + Arrays.toString(rolesAllowed) +
            ", route='" + route + '\'' +
            ", lazy=" + lazy +
            ", register=" + register +
            ", menu=" + menu +
            ", routeParameters=" + routeParameters +
            '}';
    }
}
