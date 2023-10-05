package dev.hilla.parser.core.security.classlevel;

import dev.hilla.parser.core.security.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class SomeEndpoint extends ParentEndpoint {

}
