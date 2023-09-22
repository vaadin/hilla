package dev.hilla.parser.plugins.subtypes;

@Endpoint
public class SubTypesEndpoint {

    public BaseEvent sendEvent() {
        return new AddEvent();
    }

    public void receiveEvent(BaseEvent event) {
    }
}
