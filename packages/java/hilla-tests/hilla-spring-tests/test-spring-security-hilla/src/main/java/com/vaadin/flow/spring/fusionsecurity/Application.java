package com.vaadin.flow.spring.fusionsecurity;

import com.vaadin.flow.component.dependency.NpmPackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@NpmPackage(value = "@adobe/lit-mobx",
            version = "2.0.0-rc.4")
@NpmPackage(value = "mobx",
            version = "^6.1.5")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
