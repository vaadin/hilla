package com.vaadin.hilla.parser.plugins.subtypes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = AddEvent.class, name = "add"),
        @JsonSubTypes.Type(value = UpdateEvent.class, name = "update"),
        @JsonSubTypes.Type(value = DeleteEvent.class, name = "delete") })
public class BaseEvent {
    public int id;
}
