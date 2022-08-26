package dev.hilla.parser.plugins.backbone;

import java.util.Queue;

import dev.hilla.parser.models.ClassInfoModel;

import io.swagger.v3.oas.models.OpenAPI;

final class Context {
    private final AssociationMap associationMap;
    private final Queue<ClassInfoModel> dependencies = new DependencyManager();
    private final OpenAPI openAPI;

    public Context(OpenAPI openAPI, AssociationMap associationMap) {
        this.associationMap = associationMap;
        this.openAPI = openAPI;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public Queue<ClassInfoModel> getDependencies() {
        return dependencies;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }
}
