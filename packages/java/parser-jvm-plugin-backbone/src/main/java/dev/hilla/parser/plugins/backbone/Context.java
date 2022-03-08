package dev.hilla.parser.plugins.backbone;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.core.ReplaceMap;

final class Context {
    private final AssociationMap associationMap;
    private final ReplaceMap replaceMap;

    public Context(AssociationMap associationMap, ReplaceMap replaceMap) {
        this.associationMap = associationMap;
        this.replaceMap = replaceMap;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public ReplaceMap getReplaceMap() {
        return replaceMap;
    }
}
