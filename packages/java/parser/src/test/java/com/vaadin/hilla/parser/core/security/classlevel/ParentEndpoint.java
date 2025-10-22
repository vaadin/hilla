package com.vaadin.hilla.parser.core.security.classlevel;

import com.vaadin.hilla.parser.core.security.EndpointExposed;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("admin")
@EndpointExposed
public class ParentEndpoint {

    public String getSensitiveData() {
        return "sensitive data";
    }

}
