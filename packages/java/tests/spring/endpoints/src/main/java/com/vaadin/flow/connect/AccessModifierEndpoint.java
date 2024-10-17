package com.vaadin.flow.connect;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.PermitAll;

/**
 * Simple Vaadin Connect Service definition.
 */
@Endpoint
@AnonymousAllowed
class AccessModifierEndpoint {

    @PermitAll
    public ObjectWithDifferentAccessMethods getEntity() {
        return new ObjectWithDifferentAccessMethods("private", "protected",
                "public", "package-private", "public-getter", "public-setter");
    }
}
