package com.vaadin.hilla.test.reactgrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme("react-grid-test")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Primary
    ObjectMapper myObjectMapper() {
        // This is only to test that you can have a custom object mapper without
        // causing problems for Hilla
        return new ObjectMapper();
    }

}
