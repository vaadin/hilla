package com.vaadin.hilla.push.messages.toclient;

public class ClientMessageError extends AbstractClientMessage {

    private String message;

    public ClientMessageError() {
        super();
    }

    public ClientMessageError(String id, String message) {
        super(id);
        this.message = message;

    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ClientMessageError  [id=" + getId() + ", message="
                + this.message + "]";
    }

}
