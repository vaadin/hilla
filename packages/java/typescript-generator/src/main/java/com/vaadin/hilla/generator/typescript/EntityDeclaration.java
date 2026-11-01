/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.generator.typescript;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.PropertyModel;
import com.vaadin.hilla.parser.core.Output;

/**
 * The file declaring one of the types the endpoints refer to, as it is rendered
 * while the walk runs, and printed once it is over.
 *
 * <p>
 * The declaration writes every property as one which is always there. Whether a
 * property can be absent is the work of the plugin deciding it, which marks the
 * property {@link #markOptional optional} as it contributes to the file.
 */
public final class EntityDeclaration implements Output.Document {
    private static final String BEAN = """
            interface {{declaration}} {
              {{properties}}
            }

            export default {{name}};""";

    private static final String ENUMERATION = """
            enum {{name}} {
              {{constants}}
            }

            export default {{name}};""";

    private final EntityModel entity;
    private final Set<String> optionalProperties = new HashSet<>();

    public EntityDeclaration(EntityModel entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    /**
     * The path of the file, relative to the output folder.
     */
    public String path() {
        return ModulePaths.moduleOf(entity.javaClass()) + ".ts";
    }

    /**
     * The entity declared.
     */
    public EntityModel entity() {
        return entity;
    }

    /**
     * Writes a property as one which can be absent, with the {@code ?} marker
     * rather than as a union with {@code undefined}, which is what an object
     * type says it with.
     *
     * @param name
     *            the name of the property, as the model has it
     */
    public void markOptional(String name) {
        optionalProperties.add(Objects.requireNonNull(name));
    }

    @Override
    public String print() {
        var module = ModulePaths.moduleOf(entity.javaClass());

        var file = switch (entity) {
        case EntityModel.Bean bean -> print(module, bean);
        case EntityModel.Enumeration enumeration -> print(module, enumeration);
        };

        return file.content();
    }

    private GeneratedFile print(String module, EntityModel.Bean bean) {
        var name = ModulePaths.entityName(bean.javaClass());
        var imports = new ImportRegistry();

        // The declaration is named by the Java class, so an import of the same
        // name is the one which gives way
        imports.reserve(name);

        var types = new TypeWriter(imports,
                ModulePaths.directoryOf(bean.javaClass()));

        // A type the browser has is written as it is, so no import may take
        // the name of one
        bean.properties()
                .forEach(property -> types.reserveProvided(property.type()));

        var declaration = name + typeParameters(bean) + extended(bean, types);

        var properties = Stream
                .concat(bean.properties().stream()
                        .map(property -> writeProperty(property, types)),
                        bean.discriminator().stream()
                                .map(EntityDeclaration::writeDiscriminator))
                .collect(Collectors.joining("\n"));

        var body = Template.of(BEAN) //
                .with("declaration", declaration) //
                .with("properties", properties) //
                .with("name", name) //
                .fill();

        return GeneratedFile.of(module, imports, body);
    }

    private static GeneratedFile print(String module,
            EntityModel.Enumeration enumeration) {
        var name = ModulePaths.entityName(enumeration.javaClass());

        var constants = enumeration.constants().stream()
                .map(constant -> constant + " = '" + constant + "',")
                .collect(Collectors.joining("\n"));

        return GeneratedFile.of(module, new ImportRegistry(),
                Template.of(ENUMERATION) //
                        .with("name", name) //
                        .with("constants", constants) //
                        .fill());
    }

    private String writeProperty(PropertyModel property, TypeWriter types) {
        return Names.property(property.name())
                + (optionalProperties.contains(property.name()) ? "?" : "")
                + ": " + types.writeRequired(property.type()) + ";";
    }

    /**
     * Writes the property saying which subtype a value is, which holds one of
     * the ids of the hierarchy rather than any string: that is what lets
     * TypeScript tell a value of one subtype from a value of another.
     */
    private static String writeDiscriminator(
            EntityModel.Discriminator discriminator) {
        return Names.property(discriminator.name())
                + ": " + discriminator.acceptedValues().stream()
                        .map(Names::string).collect(Collectors.joining(" | "))
                + ";";
    }

    /**
     * Writes the type parameters of the declaration, each of them defaulting to
     * an unknown type, so that the declaration can also be referred to without
     * saying what it is used with.
     */
    private static String typeParameters(EntityModel.Bean bean) {
        return bean.typeParameters().isEmpty() ? ""
                : bean.typeParameters().stream()
                        .map(parameter -> parameter + " = unknown")
                        .collect(Collectors.joining(", ", "<", ">"));
    }

    private static String extended(EntityModel.Bean bean, TypeWriter types) {
        if (bean.superTypes().isEmpty()) {
            return "";
        }

        return bean.superTypes().stream().map(types::writeRequired)
                .collect(Collectors.joining(", ", " extends ", ""));
    }
}
