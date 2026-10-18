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
import java.util.List;

import com.vaadin.hilla.generator.model.EntityModel;

/**
 * Writes the model a form binds a type through, which is what tells a field
 * which property it edits and what the value of it has to be.
 *
 * <p>
 * The model of a type is a constant built with the model library: a model of an
 * object with a property per property of the type, holding the model of the
 * value of that property, or a model of an enum. A type alias of the same name
 * goes with the constant, so that the default export can be used both as a
 * value and as a type. The property saying which subtype a value of a
 * polymorphic type is goes last, pinned to the ids the type accepts.
 */
public final class FormModelWriter {
    /**
     * The suffix both the file and the constant of a model go by, on top of the
     * name of the type the model is of.
     */
    public static final String SUFFIX = "Model";

    /**
     * A model of an object, with each call of the builder on a line of its own.
     */
    private static final String OBJECT = """
            const {{name}} = {{m}}
              {{calls}};
            type {{name}} = typeof {{name}};

            export default {{name}};""";

    private static final String ENUMERATION = """
            const {{name}} = {{m}}.enum({{entity}}, {{entityName}});
            type {{name}} = typeof {{name}};

            export default {{name}};""";

    private final ModelCycles cycles;

    /**
     * @param entities
     *            every type a model is written for in the same run, which
     *            decides the references between the models that have to be read
     *            lazily
     */
    public FormModelWriter(List<EntityModel> entities) {
        this.cycles = ModelCycles.of(entities);
    }

    public GeneratedFile write(EntityModel entity) {
        var name = ModulePaths.entityName(entity.javaClass()) + SUFFIX;
        var imports = new ImportRegistry();
        imports.reserve(name);

        var models = new ModelWriter(imports,
                ModulePaths.directoryOf(entity.javaClass()), entity.javaClass(),
                cycles);

        var body = switch (entity) {
        case EntityModel.Bean bean -> write(bean, name, imports, models);
        case EntityModel.Enumeration enumeration ->
            write(enumeration, name, imports, models);
        };

        return GeneratedFile.of(
                ModulePaths.moduleOf(entity.javaClass() + SUFFIX), imports,
                body);
    }

    /**
     * The model of a type with properties, built on the model of the type it
     * inherits properties from, so that the inherited ones are bound as well.
     */
    private static String write(EntityModel.Bean bean, String name,
            ImportRegistry imports, ModelWriter models) {
        var entity = imports.importDefault(
                ModulePaths.forEntity(bean.javaClass(),
                        ModulePaths.directoryOf(bean.javaClass())),
                ModulePaths.entityName(bean.javaClass()), true);

        var calls = new ArrayList<String>();
        bean.superTypes().stream().findFirst().ifPresent(superType -> calls
                .add(".extend(" + models.base(superType) + ")"));
        calls.add(".object<" + entity + ">("
                + Names.string(ModulePaths.entityName(bean.javaClass())) + ")");
        bean.properties()
                .forEach(property -> calls
                        .add(".property(" + Names.string(property.name()) + ", "
                                + models.model(property.type()) + ")"));
        bean.discriminator()
                .ifPresent(discriminator -> calls.add(
                        ".property(" + Names.string(discriminator.name()) + ", "
                                + discriminator(discriminator, models) + ")"));
        calls.add(".build()");

        return Template.of(OBJECT) //
                .with("name", name) //
                .with("m", models.m()) //
                .with("calls", String.join("\n", calls)) //
                .fill();
    }

    /**
     * The model of the property saying which subtype a value is, pinned to the
     * ids the type accepts, so that the empty value of a subtype says which
     * subtype it is, which the server needs to read it back.
     */
    private static String discriminator(EntityModel.Discriminator discriminator,
            ModelWriter models) {
        var literals = discriminator.acceptedValues().stream().map(
                value -> models.m() + ".literal(" + Names.string(value) + ")")
                .toList();

        return literals.size() == 1 ? literals.get(0)
                : models.m() + ".union(" + String.join(", ", literals) + ")";
    }

    /**
     * The model of an enum, which is told the enum itself rather than its type,
     * since it is the constants of the enum which the form accepts.
     */
    private static String write(EntityModel.Enumeration enumeration,
            String name, ImportRegistry imports, ModelWriter models) {
        var entity = imports
                .importDefault(
                        ModulePaths.forEntity(enumeration.javaClass(),
                                ModulePaths
                                        .directoryOf(enumeration.javaClass())),
                        ModulePaths.entityName(enumeration.javaClass()), false);

        return Template.of(ENUMERATION) //
                .with("name", name) //
                .with("m", models.m()) //
                .with("entity", entity) //
                .with("entityName",
                        Names.string(ModulePaths
                                .entityName(enumeration.javaClass()))) //
                .fill();
    }
}
