package com.vaadin.hilla.endpoints;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vaadin.hilla.Endpoint;

/**
 * PackagePrivateEndpoint, created on 03/12/2020 18.22
 *
 * @author nikolaigorokhov
 */
@Endpoint
@AnonymousAllowed
class PackagePrivateEndpoint {

    public PackagePrivateEndpoint() {
    }

    public String getRequest() {
        return "Hello";
    }
}
