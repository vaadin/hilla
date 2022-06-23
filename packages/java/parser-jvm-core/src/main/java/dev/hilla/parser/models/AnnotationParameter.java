package dev.hilla.parser.models;

import io.github.classgraph.AnnotationParameterValue;

public final class AnnotationParameter {
    private final String name;
    private final Object value;

    public AnnotationParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public AnnotationParameter(AnnotationParameterValue parameter) {
        this(parameter.getName(), parameter.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameter)) {
            return false;
        }

        var other = (AnnotationParameter) obj;

        return name.equals(other.name) && value.equals(other.value);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + 7 * value.hashCode();
    }
}
