/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.parser.plugins.backbone.complexhierarchy;

import com.vaadin.hilla.parser.plugins.backbone.complexhierarchy.models.ComplexHierarchyModel;
import com.vaadin.hilla.parser.plugins.backbone.complexhierarchy.models.ComplexHierarchyParentEndpoint;

@Endpoint
public class ComplexHierarchyEndpoint extends ComplexHierarchyParentEndpoint {
    private EndpointDependencyToIgnore fieldToIgnore;

    // Using ComplexHierarchyModel from another package is intentional here to
    // verify the generator's parsing logic for that case
    public ComplexHierarchyModel getModel() {
        return new ComplexHierarchyModel();
    }

    public static class EndpointDependencyToIgnore {
    }
}
