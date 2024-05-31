package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SignalQueue<V extends BaseJsonNode> extends EventQueue<JsonEvent> {
    public enum RootType {
        LIST, VALUE;
    }

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

    public SignalQueue(V defaultValue) {
        this.mapper = new ObjectMapper();

        V rootValue;
        if (defaultValue != null) {
            rootValue = defaultValue;
        } else {
            rootValue = createListRootValue(null, null);
        }
        entries.put(EventQueue.ROOT,
                new Entry<>(EventQueue.ROOT, null, null, rootValue));
    }

    @Override
    protected void processEvent(JsonEvent event) {
        UUID id = event.getId();
        ObjectNode json = event.getJson();

        if (json.has("conditions")) {
            ArrayNode conditions = (ArrayNode) json.get("conditions");
            for (int i = 0; i < conditions.size(); i++) {
                JsonNode condition = conditions.get(i);
                Entry<V> entry = entry(condition.get("id"));
                if (entry == null) {
                    // Condition not satisfied if it references a missing node
                    return;
                }

                if (condition.has("value") && !Objects
                        .equals(condition.get("value"), entry.value)) {
                    return;
                }
            }
        }

        if (json.has("set")) {
            Entry<V> entry = entry(json.get("set"));
            if (entry == null) {
                // Ignore request for entry that might just have been removed
                return;
            }
            entry.value = (V) json.get("value");
        } else if (json.has("remove")) {
            Entry<V> parent = entry(json.get("parent"));
            Entry<V> entry = entry(json.get("remove"));

            if (parent == null || entry == null) {
                return;
            }

            if (entry.prev != null) {
                entries.get(entry.prev).next = entry.next;
            } else {
                ObjectNode listEntry = (ObjectNode) parent.value;
                listEntry.put("head", toStringOrNull(entry.next));
            }

            if (entry.next != null) {
                entries.get(entry.next).prev = entry.prev;
            } else {
                ObjectNode listEntry = (ObjectNode) parent.value;
                listEntry.put("tail", toStringOrNull(entry.prev));
            }

            // XXX: Also detach any children
            entries.remove(entry.id);
        } else if (json.has("direction")) {
            // Insert event
            Entry<V> listEntry = entry(json.get("entry"));
            String direction = json.get("direction").asText();
            UUID referenceId = uuidOrNull(json.get("reference"));
            V value = (V) json.get("value");

            if (listEntry == null) {
                return;
            }

            ObjectNode listRoot = (ObjectNode) listEntry.value;

            UUID prev = null;
            UUID next = null;
            if ("AFTER".equals(direction)) {
                prev = referenceId != null ? referenceId
                        : uuidOrNull(listRoot.get("tail"));
                if (prev != null) {
                    Entry<V> prevEntry = entries.get(prev);
                    if (prevEntry == null) {
                        return;
                    }
                    next = prevEntry.next;
                }
            } else {
                next = referenceId != null ? referenceId
                        : uuidOrNull(listRoot.get("head"));
                if (next != null) {
                    Entry<V> nextEntry = entries.get(next);
                    if (nextEntry == null) {
                        return;
                    }
                    prev = nextEntry.prev;
                }
            }

            Entry<V> newEntry = new Entry<>(id, prev, next, value);
            entries.put(id, newEntry);

            if (next != null) {
                entries.get(next).prev = id;
            } else {
                listRoot.put("tail", id.toString());
            }

            if (prev != null) {
                entries.get(prev).next = id;
            } else {
                listRoot.put("head", id.toString());
            }
        } else {
            throw new RuntimeException("Unsupported JSON: " + json.toString());
        }

        List<Entry<V>> state = new ArrayList<>();
        UUID key = uuidOrNull(entries.get(EventQueue.ROOT).value.get("head"));
        while (key != null) {
            Entry<V> entry = entries.get(key);
            state.add(entry);
            key = entry.next;
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

    private V createListRootValue(UUID head, UUID tail) {
        ObjectNode node = mapper.createObjectNode();
        node.put("head", toStringOrNull(head));
        node.put("tail", toStringOrNull(tail));
        return (V) node;
    }

    protected ObjectMapper mapper() {
        return mapper;
    }

    public UUID getId() {
        return id;
    }
}
