package com.vaadin.hilla.push.messages.fromclient;

public class SendMessage<T> extends AbstractServerMessage {
    private T content;

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
