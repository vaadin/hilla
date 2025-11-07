/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        // Keys from non-lazy views should be in the main chunk
        checkKeys("indexhtml", Mode.CONTAINS, "basic.form.name.label",
                "basic.form.address.label");
        // Keys from ViewConfig should also be in the main chunk
        checkKeys("indexhtml", Mode.CONTAINS, "viewtitle.basic",
                "viewdescription.basic", "viewtitle.lazy",
                "viewdescription.lazy");
        // Keys from the index.html file should also be in the main chunk
        checkKeys("indexhtml", Mode.CONTAINS, "viewtitle.home",
                "viewdescription.home", "home.intro");
        // Keys from the layout should also be in the main chunk
        checkKeys("indexhtml", Mode.CONTAINS, "layout.title");
    }

    @Test
    public void shouldPutKeysInSeparatedChunkForLazyView() {
        // Keys from lazy views should be in a separate chunk
        // (but not those from its ViewConfig)
        checkKeys("lazy", Mode.EQUALS, "lazy.intro", "lazy.button.label");
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
