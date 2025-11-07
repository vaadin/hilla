package com.vaadin.hilla.parser.plugins.backbone.generics;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import java.util.Map;

@Endpoint
public class GenericsExtendedEndpoint<T extends Map<?, ?>> {
    public T getMap(T map) {
        return map;
    }
}
