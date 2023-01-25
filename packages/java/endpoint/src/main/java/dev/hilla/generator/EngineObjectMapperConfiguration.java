package dev.hilla.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.ByteArrayModule;
import dev.hilla.ConditionalOnFeatureFlag;
import dev.hilla.EndpointController;

@Configuration
@ConditionalOnFeatureFlag(EngineObjectMapperConfiguration.HILLA_ENGINE_FEATURE_FLAG)
public class EngineObjectMapperConfiguration {
    private static final Logger logger = LoggerFactory
            .getLogger(EngineObjectMapperConfiguration.class);
    static final String HILLA_ENGINE_FEATURE_FLAG = "hillaEngine";

    @Bean
    @Qualifier(EndpointController.VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER)
    public ObjectMapper createObjectMapper(ApplicationContext context) {
        var builder = context.getBean(Jackson2ObjectMapperBuilder.class);
        var objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(new ByteArrayModule());

        logger.debug("Using Hilla Multi-Module Engine ObjectMapper");

        return new ObjectMapper();
    }
}
