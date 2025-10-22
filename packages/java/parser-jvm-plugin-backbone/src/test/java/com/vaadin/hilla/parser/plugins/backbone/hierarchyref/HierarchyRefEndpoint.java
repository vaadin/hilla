package com.vaadin.hilla.parser.plugins.backbone.hierarchyref;

import java.util.List;
import java.util.Map;

@Endpoint
public class HierarchyRefEndpoint {
    public HierarchyRef getHierarchyRef(List<Map<String, String>> data) {
        return new HierarchyRef();
    }
}
