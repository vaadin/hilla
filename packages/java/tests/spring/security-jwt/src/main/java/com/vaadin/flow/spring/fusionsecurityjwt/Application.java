/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.spring.fusionsecurityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
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
