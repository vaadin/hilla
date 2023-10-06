package dev.hilla.parser.core.security.classlevel;

import dev.hilla.parser.core.security.EndpointExposed;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("admin")
@EndpointExposed
public class ParentEndpoint {

    public String getSensitiveData() {
        return "sensitive data";
    }

}
