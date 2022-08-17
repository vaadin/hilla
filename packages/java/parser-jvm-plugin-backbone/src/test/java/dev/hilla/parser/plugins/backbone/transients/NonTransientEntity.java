package dev.hilla.parser.plugins.backbone.transients;

public class NonTransientEntity {
    private String entityField;
    private transient String transientEntityField;

    public String getEntityField() {
        return entityField;
    }

    public void setEntityField(String entityField) {
        this.entityField = entityField;
    }

    public String getTransientEntityField() {
        return transientEntityField;
    }

    public void setTransientEntityField(String transientEntityField) {
        this.transientEntityField = transientEntityField;
    }
}
