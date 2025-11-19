package com.vaadin.hilla.route;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import com.vaadin.flow.internal.hilla.FileRouterRequestUtil;

@AutoConfiguration
class FileRouterRequestUtilConfiguration {

    @Bean
    @ConditionalOnMissingBean
    FileRouterRequestUtil hillaFileRouterRequestUtil() {
        return request -> {
            return false;
        };
    }
}
