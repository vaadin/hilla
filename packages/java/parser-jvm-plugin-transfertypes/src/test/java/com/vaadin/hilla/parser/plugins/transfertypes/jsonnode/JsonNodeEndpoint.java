package com.vaadin.hilla.parser.plugins.transfertypes.jsonnode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Endpoint
public class JsonNodeEndpoint {
    public JsonNode jsonNode(JsonNode node) {
        return node;
    }

    public ObjectNode objectNode(ObjectNode node) {
        return node;
    }

    public ArrayNode arrayNode(ArrayNode node) {
        return node;
    }
}
