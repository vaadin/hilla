package com.vaadin.hilla.parser.plugins.subtypes;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class SubTypesEndpoint {

    public BaseEvent sendEvent() {
        return new AddEvent();
    }

    public void receiveEvent(BaseEvent event) {
    }
}
