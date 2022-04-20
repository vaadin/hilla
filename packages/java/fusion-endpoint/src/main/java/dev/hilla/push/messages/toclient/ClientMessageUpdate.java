package dev.hilla.push.messages.toclient;

public class ClientMessageUpdate extends AbstractClientMessage {
    private Object item;

    public ClientMessageUpdate() {
        super();
    }

    public ClientMessageUpdate(String id, Object item) {
        super(id);
        this.item = item;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "ClientMessageUpdate [id=" + getId() + ", item=" + item + "]";
    }

}
