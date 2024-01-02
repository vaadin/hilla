package com.vaadin.flow.connect;

import com.vaadin.hilla.Endpoint;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.Location;

/**
 * Endpoint referencing data types known to work with Hilla TypeScript
 * generator.
 */
@Endpoint
@AnonymousAllowed
public class KnownTypesEndpoint {
    /**
     * @see https://github.com/vaadin/hilla/issues/1724
     * @return
     */
    public Location getLocation() {
        return new Location("/");
    }
}
