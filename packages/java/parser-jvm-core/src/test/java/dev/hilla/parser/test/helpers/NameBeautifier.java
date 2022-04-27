package dev.hilla.parser.test.helpers;

public interface NameBeautifier {
    String getPart();

    default String restore(String name) {
        return getPart() + name;
    }

    default String shorten(String name) {
        return name.substring(getPart().length());
    }
}
