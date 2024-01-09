package com.vaadin.hilla.push.messages.toclient;

public class ClientMessageComplete extends AbstractClientMessage {
    public ClientMessageComplete() {
    }

    public ClientMessageComplete(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return "ClientMessageComplete  [id=" + getId() + "]";
    }

}
