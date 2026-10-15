/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.parser.plugins.backbone.complexhierarchy.models;

public class ComplexHierarchyModel extends ComplexHierarchyParentModel {
    private String name;

    public String getName() {
        return name;
    }

    public EntityDependencyToIgnore methodToIgnore(
            EntityDependencyToIgnore parameter) {
        return parameter;
    }

    public static class EntityDependencyToIgnore {
    }
}
