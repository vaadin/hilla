package dev.hilla.parser.core.dependency;

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
