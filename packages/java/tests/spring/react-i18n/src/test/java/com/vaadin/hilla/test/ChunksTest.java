package com.vaadin.hilla.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunksTest {

    @Test
    public void shouldPutKeysInMainChunkForNonLazyView() throws IOException {
        var mapper = new ObjectMapper();
        var root = mapper.readTree(
                getClass().getResource("/META-INF/VAADIN/config/i18n.json"));
        var keys = root.path("chunks").path("indexhtml").path("keys");
        Assert.assertTrue(keys.isArray());
        var keyTexts = new java.util.HashSet<String>();
        for (var key : keys) {
            keyTexts.add(key.asText());
        }
        Assert.assertTrue("Missing 'basic.form.name.label'",
                keyTexts.contains("basic.form.name.label"));
        Assert.assertTrue("Missing 'basic.form.address.label'",
                keyTexts.contains("basic.form.address.label"));
    }

    @Test
    public void shouldPutKeysInSeparatedChunkForLazyView() throws IOException {
        var mapper = new ObjectMapper();
        var root = mapper.readTree(
                getClass().getResource("/META-INF/VAADIN/config/i18n.json"));
        var keys = root.path("chunks").path("lazy").path("keys");
        Assert.assertTrue(keys.isArray());
        var expected = mapper
                .readTree("[\"lazy.intro\", \"lazy.button.label\"]");
        Assert.assertEquals("Keys array does not match expected", expected,
                keys);
    }
}
