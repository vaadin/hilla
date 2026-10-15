/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.route.records;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the type of route parameter for the client side or the server side
 * views
 */
public enum RouteParamType {
    @JsonProperty("req")
    REQUIRED, @JsonProperty("opt")
    OPTIONAL, @JsonProperty("*")
    WILDCARD
}
