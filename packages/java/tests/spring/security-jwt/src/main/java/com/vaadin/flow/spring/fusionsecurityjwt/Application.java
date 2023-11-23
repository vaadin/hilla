package com.vaadin.flow.spring.fusionsecurityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.vaadin.flow.component.dependency.NpmPackage;

@SpringBootApplication
@ComponentScan(basePackages = { "com.vaadin.flow.spring.fusionsecurity",
        "com.vaadin.flow.spring.fusionsecurityjwt" }, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.vaadin\\.flow\\.spring\\.fusionsecurity\\.endpoints\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.vaadin.flow.spring.fusionsecurity.Application") })
@EntityScan(basePackages = { "com.vaadin.flow.spring.fusionsecurity.data" })
@Import(JwtSecurityUtils.class)
@NpmPackage(value = "@adobe/lit-mobx", version = "2.0.0-rc.4")
@NpmPackage(value = "mobx", version = "6.3.3")
@EnableJpaRepositories(basePackages = {
        "com.vaadin.flow.spring.fusionsecurity.data" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
