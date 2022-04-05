package dev.hilla.parser.plugins.backbone;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.core.MapperSet;

final class Context {
    private final AssociationMap associationMap;
    private final MapperSet mapperSet;

    public Context(AssociationMap associationMap, MapperSet mapperSet) {
        this.associationMap = associationMap;
        this.mapperSet = mapperSet;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public MapperSet getMappingRuleSet() {
        return mapperSet;
    }
}
