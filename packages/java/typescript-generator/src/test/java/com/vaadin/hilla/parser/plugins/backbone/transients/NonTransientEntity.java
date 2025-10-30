package com.vaadin.hilla.parser.plugins.backbone.transients;

public class NonTransientEntity {
    private String entityField;
    private transient String transientWithGetter;

    public String getEntityField() {
        return entityField;
    }

    public void setEntityField(String entityField) {
        this.entityField = entityField;
    }

    public String getTransientWithGetter() {
        return transientWithGetter;
    }

    public void setTransientWithGetter(String transientWithGetter) {
        this.transientWithGetter = transientWithGetter;
    }
}
