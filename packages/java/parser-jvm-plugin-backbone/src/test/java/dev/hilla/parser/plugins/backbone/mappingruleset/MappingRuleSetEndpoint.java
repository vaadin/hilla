package dev.hilla.parser.plugins.backbone.mappingruleset;

@Endpoint
public class MappingRuleSetEndpoint {
    public Replace.From direct(Replace.From entity) {
        return entity;
    }

    public ReplaceMapEntity withEntity() {
        return new ReplaceMapEntity();
    }

    public static class ReplaceMapEntity {
        Replace.From entity;
    }
}
