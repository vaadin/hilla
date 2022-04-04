package dev.hilla.parser.plugins.backbone;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.core.MappingRuleSet;

final class Context {
    private final AssociationMap associationMap;
    private final MappingRuleSet mappingRuleSet;

    public Context(AssociationMap associationMap,
            MappingRuleSet mappingRuleSet) {
        this.associationMap = associationMap;
        this.mappingRuleSet = mappingRuleSet;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public MappingRuleSet getMappingRuleSet() {
        return mappingRuleSet;
    }
}
