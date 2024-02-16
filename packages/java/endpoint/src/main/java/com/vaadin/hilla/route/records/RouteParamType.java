package com.vaadin.hilla.route.records;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the type of route parameter
 * for the client side or the server side views
 */
public enum RouteParamType {
    @JsonProperty("req")
    REQUIRED,
    @JsonProperty("opt")
    OPTIONAL,
    @JsonProperty("*")
    WILDCARD
}
