package com.vaadin.hilla.signals2.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.vaadin.signals.SignalCommand;
import com.vaadin.signals.SignalEnvironment;

class CommandUtils {

    private CommandUtils() {
        // Utility class, no instantiation
    }

    static JsonNode toJson(Object object) {
        if (object instanceof SignalCommand.SetCommand setCommand) {
            ObjectNode commandNode = SignalEnvironment.objectMapper().createObjectNode();
            commandNode.set("type", new TextNode("SetCommand"));
            commandNode.set("targetNodeId", new TextNode(setCommand.targetNodeId().toString()));
            commandNode.set("value", setCommand.value());
            commandNode.set("commandId", SignalEnvironment.objectMapper().valueToTree(setCommand.commandId()));
            return commandNode;
        }
        return SignalEnvironment.objectMapper().valueToTree(object);
    }

    static <T> T fromJson(JsonNode value) {
        try {
            Class<T> targetType = determineCommandClass(value);
            return SignalEnvironment.objectMapper().treeToValue(value,
                targetType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> Class<T> determineCommandClass(JsonNode value) {
        if (value.has("type")) {
            String type = value.get("type").asText();
            switch (type) {
                case "SetCommand" -> {
                    return (Class<T>) SignalCommand.SetCommand.class;
                }
                case "TransactionCommand" -> {
                    return (Class<T>) SignalCommand.TransactionCommand.class;
                }
                default -> throw new RuntimeException("Unknown command type: " + type);
            }
        }
        return (Class<T>) Object.class;
    }
}
