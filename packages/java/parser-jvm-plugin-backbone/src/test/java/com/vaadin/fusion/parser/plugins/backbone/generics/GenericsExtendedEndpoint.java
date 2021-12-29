package com.vaadin.fusion.parser.plugins.backbone.generics;

import java.util.Map;

@Endpoint
public class GenericsExtendedEndpoint<T extends Map<?, ?>> {
    public T getMap(T map) {
        return map;
    }
}
