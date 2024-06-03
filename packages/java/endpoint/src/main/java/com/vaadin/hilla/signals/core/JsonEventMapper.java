package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.UUID;

public class JsonEventMapper {

    private final ObjectMapper mapper;

    public JsonEventMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(JsonEvent jsonEvent) {
        ObjectNode root = mapper.createObjectNode();
        for (Map.Entry<String, JsonNode> entry : jsonEvent.getJson()
                .properties()) {
            root.set(entry.getKey(), entry.getValue());
        }
        UUID id = jsonEvent.getId();
        root.put("id", id != null ? id.toString() : null);
        return root.toString();
    }

    public JsonEvent fromJson(String json) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(json);
            UUID id = UUID.fromString(root.get("id").asText());
            root.remove("id");
            return new JsonEvent(id, root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
