package com.vaadin.hilla.parser.core.basic;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class BasicEndpoint {
    public final String foo = "FOO";

    private int bar() {
        return 10;
    }

    private static final class Baz {
    }
}
