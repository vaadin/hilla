package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.jackson.ByteArrayModule;

public class CustomFactory extends JacksonObjectMapperFactory.Json {
    @Override
    public ObjectMapper build() {
        return super.build().setVisibility(PropertyAccessor.ALL,
                JsonAutoDetect.Visibility.ANY);
    }
}
