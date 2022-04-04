package dev.hilla.parser.plugins.backbone.mappingruleset;

public class Replace {
    public static class Baz {

    }

    public static class From {
        String bar;
        Baz baz;
        int foo;
    }

    public static class To {
        int bar;
        Baz baz;
        String foo;
    }
}
