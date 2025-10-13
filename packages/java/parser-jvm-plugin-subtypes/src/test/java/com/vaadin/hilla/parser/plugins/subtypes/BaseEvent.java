package com.vaadin.hilla.parser.plugins.subtypes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({ @JsonSubTypes.Type(value = BaseEvent.class, name = "base"),
    @JsonSubTypes.Type(value = AddEvent.class, name = "add"),
    @JsonSubTypes.Type(value = UpdateEvent.class, name = "update"),
    @JsonSubTypes.Type(value = DeleteEvent.class, name = "delete"),
    @JsonSubTypes.Type(value = AdvancedAddEvent.class, name = "advanced-add") })
public class BaseEvent {
    public int id;
}
