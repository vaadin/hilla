package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;

public class CustomFactory extends JacksonObjectMapperFactory.Json {
    @Override
    public ObjectMapper build() {
        return super.build().setVisibility(PropertyAccessor.ALL,
                JsonAutoDetect.Visibility.ANY);
    }
}
