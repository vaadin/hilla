package com.vaadin.hilla.typescript.parser.core.security.methodlevel;

import com.vaadin.hilla.typescript.parser.core.security.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class AnotherEndpoint extends ParentEndpoint {

}
