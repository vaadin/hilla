package com.invalid;

import com.vaadin.hilla.Endpoint;

@Endpoint
/**
 * This endpoint is not valid because it will not be seen as a Spring bean, yet
 * it has a constructor with parameters.
 */
public class InvalidEndpoint {

    private final String message;

    public InvalidEndpoint(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
