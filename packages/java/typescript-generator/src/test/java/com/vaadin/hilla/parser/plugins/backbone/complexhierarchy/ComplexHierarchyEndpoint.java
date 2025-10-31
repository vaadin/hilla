/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
