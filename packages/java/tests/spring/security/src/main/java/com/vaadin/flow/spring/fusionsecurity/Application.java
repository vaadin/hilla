package com.vaadin.flow.spring.fusionsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.vaadin.flow.component.dependency.NpmPackage;

@SpringBootApplication
@NpmPackage(value = "@adobe/lit-mobx", version = "2.2.2")
@NpmPackage(value = "mobx", version = "6.13.7")
@ComponentScan(basePackages = { "com.vaadin.flow.spring.fusionsecurity",
        "com.vaadin.flow.spring.fusionsecurityjwt", }, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.vaadin.flow.spring.fusionsecurityjwt.endpoints.*") })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
