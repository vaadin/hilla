package com.vaadin.flow.spring.fusionsecurity.endpoints;

import java.time.LocalDateTime;

import com.vaadin.hilla.Endpoint;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Endpoint
@AnonymousAllowed
public class PublicEndpoint {

    public String getServerTime() {
        return LocalDateTime.now().toString();
    }
}
