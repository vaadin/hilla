package dev.hilla.crud;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;

@Configuration
public class CrudConfiguration {

    @Bean
    @ConditionalOnClass(Specification.class)
    JpaFilterConverter jpaFilterConverter() {
        return new JpaFilterConverter();
    }

}
