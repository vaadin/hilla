package com.vaadin.flow.spring.fusionsecurityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootApplication()
@ComponentScan(basePackages = {
        "com.vaadin.flow.spring.fusionsecurity" }, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.vaadin\\.flow\\.spring\\.fusionsecurity\\.endpoints\\..*") })
@Import(JwtSecurityUtils.class)
public class Application
        extends com.vaadin.flow.spring.fusionsecurity.Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
