package com.vaadin.flow.spring.fusionsecurityjwt;

import com.vaadin.flow.spring.fusionsecurity.SecurityConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vaadin.flow.flow.fusionsecurity"}, 
    excludeFilters = 
        {@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = SecurityConfig.class),
        @ComponentScan.Filter(type=FilterType.REGEX,
            pattern="com\\.vaadin\\.flow\\.flow\\.fusionsecurity\\.endpoints\\..*")})
public class Application extends com.vaadin.flow.spring.fusionsecurity.Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
