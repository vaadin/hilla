package dev.hilla.push.messages.fromclient;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubscribeMessage.class, name = "subscribe"),
        @JsonSubTypes.Type(value = UnsubscribeMessage.class, name = "unsubscribe") })
public abstract class AbstractServerMessage {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
