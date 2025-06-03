package com.vaadin.hilla.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunksTest {

    private JsonNode chunks;

    @Before
    public void loadChunks() throws IOException {
        chunks = new ObjectMapper()
                .readTree(
                        ChunksTest.class.getResource("/vaadin-i18n/i18n.json"))
                .path("chunks");
    }

    @Test
    public void shouldPutKeysInMainChunkForNonLazyView() {
        checkKeys("indexhtml", Mode.CONTAINS, "basic.form.name.label",
                "basic.form.address.label");
    }

    @Test
    public void shouldPutKeysInSeparatedChunkForLazyView() {
        checkKeys("lazy", Mode.EQUALS, "lazy.intro", "lazy.button.label",
                "viewtitle.lazy", "viewdescription.lazy");
    }

    private static enum Mode {
        CONTAINS, EQUALS
    }

    private void checkKeys(String chunk, Mode mode, String... expectedKeys) {
        // Find the entry whose name contains the chunk as a substring
        String entry = null;
        for (var it = chunks.fieldNames(); it.hasNext();) {
            String name = it.next();
            if (name.contains(chunk)) {
                entry = name;
                break;
            }
        }
        if (entry == null) {
            throw new AssertionError(
                    "No chunk found containing " + chunk + " in its name");
        }
        var node = chunks.path(entry).path("keys");
        Assert.assertTrue(node.isArray());

        // a set of expected keys, to be removed as we find them
        var remaining = new HashSet<>(Arrays.asList(expectedKeys));

        // Collect keys from the json and remove the expected ones
        var unexpected = new ArrayList<String>();
        for (var it = node.elements(); it.hasNext();) {
            String key = it.next().asText();
            if (!remaining.remove(key)) {
                unexpected.add(key);
            }
        }

        Assert.assertTrue("Missing keys: " + remaining, remaining.isEmpty());

        if (mode == Mode.EQUALS && !unexpected.isEmpty()) {
            Assert.fail("Unexpected keys found: " + unexpected);
        }
    }
}
