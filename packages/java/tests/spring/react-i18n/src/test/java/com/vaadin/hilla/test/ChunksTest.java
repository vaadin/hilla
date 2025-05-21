package com.vaadin.hilla.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

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
        checkKeys("lazy", Mode.EQUALS, "lazy.intro", "lazy.button.label");
    }

    private static enum Mode {
        CONTAINS, EQUALS
    }

    private void checkKeys(String chunkName, Mode mode,
            String... expectedKeys) {
        var node = chunks.path(chunkName).path("keys");
        Assert.assertTrue(node.isArray());
        // a set of expected keys, to be removed as we find them
        var remaining = new HashSet<>(Arrays.asList(expectedKeys));
        // streams the keys from the json and removes the expected ones
        var unexpected = StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText).filter(Predicate.not(remaining::remove))
                .toList();
        Assert.assertTrue("Missing keys: " + remaining, remaining.isEmpty());
        if (mode == Mode.EQUALS && !unexpected.isEmpty()) {
            Assert.fail("Unexpected keys found: " + unexpected);
        }
    }
}
