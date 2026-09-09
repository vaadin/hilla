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

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.PropertyModel;

/**
 * Writes the file declaring one of the types the endpoints refer to.
 */
public final class EntityWriter {
    private static final String BEAN = """
            interface {{declaration}} {
            {{properties}}
            }

            export default {{name}};""";

    /**
     * A type whose properties are all inherited, or which has none at all,
     * which TypeScript still needs the braces of.
     */
    private static final String EMPTY_BEAN = """
            interface {{declaration}} {}

            export default {{name}};""";

    private static final String ENUMERATION = """
            enum {{name}} {
            {{constants}}
            }

            export default {{name}};""";

    public GeneratedFile write(EntityModel entity) {
        return new GeneratedFile(ModulePaths.fileOf(entity.javaClass()),
                switch (entity) {
                case EntityModel.Bean bean -> write(bean);
                case EntityModel.Enumeration enumeration -> write(enumeration);
                });
    }

    private static String write(EntityModel.Bean bean) {
        var name = ModulePaths.entityName(bean.javaClass());
        var imports = new ImportRegistry();

        // The declaration is named by the Java class, so an import of the same
        // name is the one which gives way
        imports.reserve(name);

        var types = new TypeWriter(imports,
                ModulePaths.directoryOf(bean.javaClass()));

        var declaration = name + typeParameters(bean) + extended(bean, types);

        var properties = Stream
                .concat(bean.properties().stream()
                        .map(property -> writeProperty(property, types)),
                        bean.discriminator().stream()
                                .map(EntityWriter::writeDiscriminator))
                .collect(Collectors.joining("\n"));

        var body = Template.of(properties.isEmpty() ? EMPTY_BEAN : BEAN) //
                .with("declaration", declaration) //
                .with("properties", properties) //
                .with("name", name) //
                .fill();

        return file(imports, body);
    }

    private static String write(EntityModel.Enumeration enumeration) {
        var name = ModulePaths.entityName(enumeration.javaClass());

        var constants = enumeration.constants().stream()
                .map(constant -> "  " + constant + " = '" + constant + "',")
                .collect(Collectors.joining("\n"));

        return file(new ImportRegistry(), Template.of(ENUMERATION) //
                .with("name", name) //
                .with("constants", constants) //
                .fill());
    }

    /**
     * Writes a property as optional with the {@code ?} marker rather than as a
     * union with {@code undefined}, which is what an object type says it with.
     */
    private static String writeProperty(PropertyModel property,
            TypeWriter types) {
        return "  " + property.name() + (property.type().optional() ? "?" : "")
                + ": " + types.writeRequired(property.type()) + ";";
    }

    /**
     * Writes the property saying which subtype a value is, which holds one of
     * the ids of the hierarchy rather than any string: that is what lets
     * TypeScript tell a value of one subtype from a value of another.
     */
    private static String writeDiscriminator(
            EntityModel.Discriminator discriminator) {
        return "  " + Names.property(discriminator.name()) + ": "
                + discriminator.acceptedValues().stream()
                        .map(value -> "'" + value + "'")
                        .collect(Collectors.joining(" | "))
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

    private static String file(ImportRegistry imports, String body) {
        var lines = new ArrayList<>(imports.write());

        if (!lines.isEmpty()) {
            lines.add("");
        }

        lines.add(body);

        return String.join("\n", lines) + "\n";
    }
}
