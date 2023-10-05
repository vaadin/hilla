package dev.hilla.parser.core.security.methodlevel;

import dev.hilla.parser.core.security.EndpointExposed;
import jakarta.annotation.security.RolesAllowed;

@EndpointExposed
public class ParentEndpoint {

    @RolesAllowed("admin")
    public String getSensibleData() {
        return "sensitive data";
    }
}
