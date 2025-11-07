package com.vaadin.hilla.parser.core.security.methodlevel;

import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;
import jakarta.annotation.security.RolesAllowed;

@EndpointExposed
public class ParentEndpoint {

    @RolesAllowed("admin")
    public String getSensibleData() {
        return "sensitive data";
    }
}
