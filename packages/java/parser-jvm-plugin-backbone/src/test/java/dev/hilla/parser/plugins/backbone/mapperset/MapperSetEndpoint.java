package dev.hilla.parser.plugins.backbone.mapperset;

@Endpoint
public class MapperSetEndpoint {
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
