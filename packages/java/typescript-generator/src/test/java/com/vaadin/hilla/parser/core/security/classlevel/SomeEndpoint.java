package com.vaadin.hilla.parser.core.security.classlevel;

import com.vaadin.hilla.parser.core.security.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class SomeEndpoint extends ParentEndpoint {

}
