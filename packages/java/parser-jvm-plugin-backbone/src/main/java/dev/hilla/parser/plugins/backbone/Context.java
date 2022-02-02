package dev.hilla.parser.plugins.backbone;

import java.util.HashSet;
import java.util.Set;

import dev.hilla.parser.core.AssociationMap;

class Context {
    private final AssociationMap associationMap;
    private final Set<String> refs = new HashSet<>();

    public Context(AssociationMap associationMap) {
        this.associationMap = associationMap;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public Set<String> getRefs() {
        return refs;
    }
}
