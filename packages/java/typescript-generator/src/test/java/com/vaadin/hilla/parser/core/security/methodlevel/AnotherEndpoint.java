package com.vaadin.hilla.parser.core.security.methodlevel;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class AnotherEndpoint extends ParentEndpoint {

}
