package com.vaadin.fusion.parser.plugins.backbone.generics;

import java.util.ArrayList;
import java.util.List;

public class GenericsRefEntity<T> {
    private T property;

    public <U extends List<String>> U getList() {
        return (U) new ArrayList<String>();
    }

    public <U extends GenericsRefEntity<String>> U getRef() {
        return (U) new GenericsRefEntity<String>();
    }

    public <U> U getValueWithAnotherGenericType() {
        return (U) new Object();
    }

    public T getValueWithGenericType() {
        return property;
    }
}
