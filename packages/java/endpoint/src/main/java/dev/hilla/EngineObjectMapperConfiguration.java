package dev.hilla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@Configuration
@ConditionalOnFeatureFlag(EngineObjectMapperConfiguration.HILLA_ENGINE_FEATURE_FLAG)
public class EngineObjectMapperConfiguration {
    private static final Logger logger = LoggerFactory
            .getLogger(EngineObjectMapperConfiguration.class);
    static final String HILLA_ENGINE_FEATURE_FLAG = "hillaEngine";

    @Bean
    @Qualifier(EndpointController.VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER)
    public ObjectMapper createObjectMapper(ApplicationContext context) {
        logger.debug("Using Hilla Multi-Module Engine ObjectMapper");

        return context.getBean(Jackson2ObjectMapperBuilder.class)
                .createXmlMapper(false).build()
                .registerModule(new ByteArrayModule())
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }
}
