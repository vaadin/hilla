package com.vaadin.hilla.route.records;

/**
 * Represents a server side view
 * configuration for the client side.
 * @param route
 * @param title
 * @param hasMandatoryParam
 */
public record ServerViewInfo(String route, String title, boolean hasMandatoryParam) {
    }
