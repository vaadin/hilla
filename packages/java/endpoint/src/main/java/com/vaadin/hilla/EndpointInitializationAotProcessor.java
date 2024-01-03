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
