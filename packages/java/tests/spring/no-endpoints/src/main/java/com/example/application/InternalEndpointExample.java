package com.example.application;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.InternalBrowserCallable;

@Endpoint
@InternalBrowserCallable
public class InternalEndpointExample {
    public String exampleMethod() {
        return null;
    }
}
