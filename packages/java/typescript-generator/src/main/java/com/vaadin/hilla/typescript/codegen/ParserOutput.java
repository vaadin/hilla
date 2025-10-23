package com.vaadin.hilla.typescript.codegen;

import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Output from the parser containing all the information needed for TypeScript
 * generation. This replaces OpenAPI as the intermediate format between parsing
 * and generation.
 */
public class ParserOutput {
    private final List<ClassInfoModel> endpoints;
    private final List<ClassInfoModel> entities;

    /**
     * Creates a new parser output.
     *
     * @param endpoints the endpoint classes (e.g., @BrowserCallable classes)
     * @param entities  the entity/model classes referenced by endpoints
     */
    public ParserOutput(@NonNull List<ClassInfoModel> endpoints,
            @NonNull List<ClassInfoModel> entities) {
        this.endpoints = new ArrayList<>(endpoints);
        this.entities = new ArrayList<>(entities);
    }

    /**
     * Gets the list of endpoint classes.
     *
     * @return the endpoints
     */
    @NonNull
    public List<ClassInfoModel> getEndpoints() {
        return new ArrayList<>(endpoints);
    }

    /**
     * Gets the list of entity/model classes.
     *
     * @return the entities
     */
    @NonNull
    public List<ClassInfoModel> getEntities() {
        return new ArrayList<>(entities);
    }

    /**
     * Gets all classes (endpoints + entities).
     *
     * @return all classes
     */
    @NonNull
    public List<ClassInfoModel> getAllClasses() {
        List<ClassInfoModel> all = new ArrayList<>(endpoints);
        all.addAll(entities);
        return all;
    }
}
