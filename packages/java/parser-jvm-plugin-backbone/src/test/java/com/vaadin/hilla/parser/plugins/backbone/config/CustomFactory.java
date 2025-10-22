package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.jackson.ByteArrayModule;

public class CustomFactory extends JacksonObjectMapperFactory.Json {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class VisibilityMixIn {
    }

    @Override
    public ObjectMapper build() {
        return JsonMapper.builder().addModule(new ByteArrayModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addMixIn(CustomConfigEndpoint.CustomConfigEntity.class,
                        VisibilityMixIn.class)
                .build();
    }
}
