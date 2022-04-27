package dev.hilla.parser.test.helpers;

public interface NameBeautifier {
    String getPart();

    default String shorten(String name) {
        return name.substring(getPart().length());
    }

    default String restore(String name) {
        return getPart() + name;
    }
}
