package com.vaadin.hilla.parser.plugins.subtypes;

@Endpoint
public class SubTypesEndpoint {

    public BaseEvent sendEvent() {
        return new AddEvent();
    }

    public void receiveEvent(BaseEvent event) {
    }
}
