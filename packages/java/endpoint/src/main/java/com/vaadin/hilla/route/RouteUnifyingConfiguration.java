package com.vaadin.hilla.route;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteUnifyingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "vaadin")
    public RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties() {
        return new RouteUnifyingConfigurationProperties();
    }
}
