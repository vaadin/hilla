/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.endpoints;

import java.util.Arrays;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vaadin.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class IterableEndpoint {

    public Iterable<Foo> getFoos() {
        return Arrays.asList(new Foo(), new Foo());
    }

    public static class Foo {
        public String bar = "bar";
    }
}
