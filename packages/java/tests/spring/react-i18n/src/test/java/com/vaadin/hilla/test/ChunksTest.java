package com.vaadin.hilla.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunksTest {

    @Test
    public void shouldPutKeysInChunk() throws IOException {
        var mapper = new ObjectMapper();
        var root = mapper.readTree(
                getClass().getResource("/META-INF/VAADIN/config/i18n.json"));
        var keys = root.path("chunks").path("basic-i18n").path("keys");
        Assert.assertTrue(keys.isArray());
        var expected = mapper.readTree(
                "[\"basic.form.name.label\", \"basic.form.address.label\"]");
        Assert.assertEquals("Keys array does not match expected", expected,
                keys);
    }
}
