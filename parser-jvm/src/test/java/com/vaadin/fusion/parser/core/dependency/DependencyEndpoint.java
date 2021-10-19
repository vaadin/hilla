package com.vaadin.fusion.parser.core.dependency;

import com.vaadin.fusion.parser.testutils.Endpoint;

@Endpoint
public class DependencyEndpoint {
    private final DependencyEntityOne entityOne = new DependencyEntityOne();
    private final DependencyEntityTwo entityTwo = new DependencyEntityTwo();

    public DependencyEntityOne getEntityOne() {
        return entityOne;
    }

    public DependencyEntityTwo getEntityTwo() {
        return entityTwo;
    }
}
