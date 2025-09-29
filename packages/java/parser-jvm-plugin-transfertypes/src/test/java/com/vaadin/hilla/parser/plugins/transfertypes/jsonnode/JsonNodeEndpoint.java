package com.vaadin.hilla.parser.plugins.transfertypes.jsonnode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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
