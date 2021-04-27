package com.vaadin.flow.spring.fusionsecurity;

public class MenuItem {

    private String href;
    private String text;
    private boolean available;

    public MenuItem(String href, String text, boolean available) {
        this.href = href;
        this.text = text;
        this.available = available;
    }

    public String getHref() {
        return href;
    }

    public String getText() {
        return text;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "MenuItem [available=" + available + ", href=" + href + ", text=" + text + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (available ? 1231 : 1237);
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MenuItem other = (MenuItem) obj;
        if (available != other.available)
            return false;
        if (href == null) {
            if (other.href != null)
                return false;
        } else if (!href.equals(other.href))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }

}