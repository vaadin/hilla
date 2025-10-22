package com.vaadin.hilla.parser.plugins.backbone.hierarchyref;

public class HierarchyRef extends HierarchyRefSuperclass {
    private HierarchyRefSuperclass child;

    public HierarchyRefSuperclass getChild() {
        return child;
    }

    public void setChild(HierarchyRefSuperclass child) {
        this.child = child;
    }
}
