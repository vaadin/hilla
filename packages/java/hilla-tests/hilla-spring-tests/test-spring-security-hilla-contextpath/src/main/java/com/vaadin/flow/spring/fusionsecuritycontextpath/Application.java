package com.vaadin.flow.spring.fusionsecuritycontextpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application extends com.vaadin.flow.spring.fusionsecurity.Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
