package com.vaadin.fusion.maven;

final class ParserClassPathConfiguration {
    private String value;
    private final boolean override = false;

    public String getValue() {
        return value;
    }

    public boolean isOverride() {
        return override;
    }
}
