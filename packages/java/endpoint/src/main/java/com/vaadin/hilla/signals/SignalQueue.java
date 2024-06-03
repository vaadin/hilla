package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.EventQueue;
import com.vaadin.hilla.signals.core.JsonEvent;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SignalQueue<V extends BaseJsonNode> extends EventQueue<JsonEvent> {

    private static class Entry<V> {
        private final UUID id;
        private UUID prev;
        private UUID next;
        private V value;

        public Entry(UUID id, UUID prev, UUID next, V value) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = value;
        }

        @Override
        public String toString() {
            return id + ": " + value;
        }
    }

    private final Map<UUID, Entry<V>> entries = new HashMap<>();
    private final ObjectMapper mapper;
    private final UUID id = UUID.randomUUID();

    public SignalQueue(@NotNull V defaultValue) {
        this.mapper = new ObjectMapper();

        entries.put(EventQueue.ROOT,
                new Entry<>(EventQueue.ROOT, null, null, defaultValue));
    }

    @Override
    protected void processEvent(JsonEvent event) {
        ObjectNode json = event.getJson();

        if (!checkConditions(json)) {
            return;
        }

        handleCommand(json);

        UUID key = uuidOrNull(entries.get(EventQueue.ROOT).value.get("head"));
        while (key != null) {
            Entry<V> entry = entries.get(key);
            key = entry.next;
        }
    }

    private boolean checkConditions(ObjectNode json) {
        if (json.has("conditions")) {
            ArrayNode conditions = (ArrayNode) json.get("conditions");
            for (int i = 0; i < conditions.size(); i++) {
                JsonNode condition = conditions.get(i);
                Entry<V> entry = entry(condition.get("id"));
                if (entry == null) {
                    // Condition not satisfied if it references a missing node
                    return false;
                }

                if (condition.has("value") && !Objects
                    .equals(condition.get("value"), entry.value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleCommand(ObjectNode json) {
        if (json.has("set")) {
            Entry<V> entry = entry(json.get("set"));
            if (entry == null) {
                // Ignore request for entry that might just have been removed
                return;
            }
            entry.value = (V) json.get("value");
        } else {
            throw new RuntimeException("Unsupported JSON: " + json.toString());
        }
    }

    private Entry<V> entry(JsonNode jsonNode) {
        return entries.get(UUID.fromString(jsonNode.asText()));
    }

    private static UUID uuidOrNull(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        } else {
            return UUID.fromString(jsonNode.asText());
        }
    }

    @Override
    public JsonEvent createSnapshot() {
        ArrayNode snapshotEntries = mapper.createArrayNode();
        entries.values().forEach(entry -> {
            ObjectNode entryNode = snapshotEntries.addObject();
            entryNode.put("id", entry.id.toString());
            entryNode.put("next", toStringOrNull(entry.next));
            entryNode.put("prev", toStringOrNull(entry.prev));
            entryNode.set("value", entry.value);
        });

        ObjectNode snapshotData = mapper.createObjectNode();
        snapshotData.set("entries", snapshotEntries);
        return new JsonEvent(null, snapshotData);
    }

    private static String toStringOrNull(UUID uuid) {
        return Objects.toString(uuid, null);
    }

    protected ObjectMapper mapper() {
        return mapper;
    }

    public UUID getId() {
        return id;
    }
}
