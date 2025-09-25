package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;

import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;

public class CustomFactory extends JacksonObjectMapperFactory.Json {
    @Override
    public ObjectMapper build() {
        var mapper = super.build();
        try {
            MapperBuilder<? extends ObjectMapper, ?> builder = mapper.rebuild();
            builder.changeDefaultVisibility(vc -> vc.withVisibility(
                    PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY));
            return builder.build();
        } catch (IllegalStateException ex) {
            return mapper;
        }
    }
}
