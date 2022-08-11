package dev.hilla.parser.plugins.backbone;

import io.swagger.v3.oas.models.OpenAPI;

final class Context {
    private final AssociationMap associationMap;
    private final String endpointAnnotationName;
    private final OpenAPI openAPI;

    public Context(OpenAPI openAPI, String endpointAnnotationName,
            AssociationMap associationMap) {
        this.associationMap = associationMap;
        this.openAPI = openAPI;
        this.endpointAnnotationName = endpointAnnotationName;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public String getEndpointAnnotationName() {
        return endpointAnnotationName;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }
}
