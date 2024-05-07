package com.vaadin.hilla.parser.plugins.transfertypes.jsonnode;

import com.fasterxml.jackson.databind.JsonNode;

@Endpoint
public class JsonNodeEndpoint {
    public JsonNode echoJsonNode(JsonNode node) {
        return node;
    }
}
