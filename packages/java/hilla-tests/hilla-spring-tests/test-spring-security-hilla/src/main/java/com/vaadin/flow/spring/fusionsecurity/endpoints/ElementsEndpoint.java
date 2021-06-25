package com.vaadin.flow.spring.fusionsecurity.endpoints;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.fusion.Endpoint;
import com.vaadin.flow.spring.fusionsecurity.fusionform.Elements;
import com.vaadin.flow.spring.fusionsecurity.fusionform.Elements.Options;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Endpoint
@AnonymousAllowed
public class ElementsEndpoint {

    public Elements getElements() {
     return new Elements();
    }

    public List<String> getOptions() {
        return Stream.of(Options.values()).map(Enum::toString).collect(Collectors.toList());
    }

    public Elements saveElements(Elements item) {
        return item;
    }
}

