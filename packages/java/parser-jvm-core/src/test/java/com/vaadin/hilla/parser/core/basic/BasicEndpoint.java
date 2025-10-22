package com.vaadin.hilla.parser.core.basic;

@Endpoint
public class BasicEndpoint {
    public final String foo = "FOO";

    private int bar() {
        return 10;
    }

    private static final class Baz {
    }
}
