package dev.hilla.parser.core.security.methodlevel;

import dev.hilla.parser.core.security.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class AnotherEndpoint extends ParentEndpoint {

}
