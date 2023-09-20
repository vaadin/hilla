package dev.hilla.parser.plugins.subtypes;

@Endpoint
public class SubTypesEndpoint {

    public Event sendEvent() {
        return new AddEvent();
    }

    public void receiveEvent(Event event) {
    }
}
