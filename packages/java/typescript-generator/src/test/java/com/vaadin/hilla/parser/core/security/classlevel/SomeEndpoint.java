package com.vaadin.hilla.parser.core.security.classlevel;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Endpoint
public class SomeEndpoint extends ParentEndpoint {

}
