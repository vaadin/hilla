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
package com.vaadin.hilla;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

class EndpointInitializationAotProcessor
        implements BeanFactoryInitializationAotProcessor {

    @Override
    public BeanFactoryInitializationAotContribution processAheadOfTime(
            ConfigurableListableBeanFactory beanFactory) {
        String[] endpoints = beanFactory
                .getBeanNamesForAnnotation(Endpoint.class);
        String[] browserCallables = beanFactory
                .getBeanNamesForAnnotation(BrowserCallable.class);
        Set<Class<?>> endpointClasses = new HashSet<>();

        for (String endpoint : endpoints) {
            endpointClasses.add(beanFactory.getType(endpoint));
        }
        for (String browserCallable : browserCallables) {
            endpointClasses.add(beanFactory.getType(browserCallable));
        }

        return new BeanFactoryInitializationAotContribution() {
            @Override
            public void applyTo(GenerationContext generationContext,
                    BeanFactoryInitializationCode beanFactoryInitializationCode) {
                for (Class<?> endpointClass : endpointClasses) {
                    generationContext.getRuntimeHints().reflection()
                            .registerType(endpointClass,
                                    MemberCategory.values());
                }
            }
        };
    }
}
