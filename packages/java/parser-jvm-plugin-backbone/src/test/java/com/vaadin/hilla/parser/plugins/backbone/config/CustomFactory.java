package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.jackson.ByteArrayModule;

public class CustomFactory extends JacksonObjectMapperFactory.Json {

    // MixIn to make private fields visible
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class VisibilityMixIn {
    }

    @Override
    public ObjectMapper build() {
        // Build Jackson 3 mapper with base configuration
        // Note: In Jackson 3, we can't directly set visibility with Jackson 2
        // enums
        // We'll use a MixIn annotation to configure visibility
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new ByteArrayModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addMixIn(CustomConfigEndpoint.CustomConfigEntity.class,
                        VisibilityMixIn.class)
                .build();

        // For a more general solution across all classes, we'd need to
        // configure
        // Jackson 3's visibility differently or use annotations on the target
        // classes

        return mapper;
    }
}
