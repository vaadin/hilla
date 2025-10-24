package com.vaadin.hilla.engine;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * Configuration for the Parser. Allows customizing endpoint annotations.
 *
 * Note: Parser plugin configuration has been removed. The parser no longer uses
 * plugins for OpenAPI generation. TypeScript is generated directly from Java
 * classes via TypeScriptGenerator.
 */
public final class ParserConfiguration {
    private List<Class<? extends Annotation>> endpointAnnotations = List.of();
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of();

    public List<Class<? extends Annotation>> getEndpointAnnotations() {
        return endpointAnnotations;
    }

    public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return endpointExposedAnnotations;
    }

    public void setEndpointAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointAnnotations) {
        this.endpointAnnotations = endpointAnnotations;
    }

    public void setEndpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointExposedAnnotations) {
        this.endpointExposedAnnotations = endpointExposedAnnotations;
    }
}
