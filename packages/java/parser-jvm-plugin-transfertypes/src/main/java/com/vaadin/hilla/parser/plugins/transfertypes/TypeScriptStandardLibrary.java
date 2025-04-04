package com.vaadin.hilla.parser.plugins.transfertypes;

interface TypeScriptStandardLibrary extends TransferType {
    static Primitive NULL = new Primitive("null");
    static Primitive BOOLEAN = new Primitive("boolean");
    static Primitive NUMBER = new Primitive("number");
    static Primitive STRING = new Primitive("string");
    static Primitive ARRAY = new Primitive("array");
    static Primitive OBJECT = new Primitive("object");

    static Type TYPE(String type) {
        return new Type(type);
    }

    record Primitive(String type) implements TypeScriptStandardLibrary {
    }

    record Type(String type) implements TypeScriptStandardLibrary {
    }
}
