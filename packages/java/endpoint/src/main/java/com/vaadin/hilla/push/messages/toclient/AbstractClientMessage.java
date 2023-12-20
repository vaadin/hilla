package com.vaadin.hilla.push.messages.toclient;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClientMessageComplete.class, name = "complete"),
        @JsonSubTypes.Type(value = ClientMessageError.class, name = "error"),
        @JsonSubTypes.Type(value = ClientMessageUpdate.class, name = "update") })
public abstract class AbstractClientMessage {

    private String id;

    protected AbstractClientMessage() {
    }

    protected AbstractClientMessage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
