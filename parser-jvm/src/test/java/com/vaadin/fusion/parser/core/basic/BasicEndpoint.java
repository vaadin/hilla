package com.vaadin.fusion.parser.core.basic;

import com.vaadin.fusion.parser.core.Endpoint;

@Endpoint
public class BasicEndpoint {
    public final String foo = "FOO";
    private int bar = 111;

    public String getFoo() {
        return foo;
    }

    protected void baz(final int id) {
    }

    private int getBar() {
        return bar;
    }

    public class Foo {
    }

    private static class Bar {
    }
}
