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

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.PropertyModel;
import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Writes the model a form binds a type through, which is what tells a field
 * which property it edits and what the value of it has to be.
 *
 * <p>
 * The model of a type mirrors the type: a class per declaration, a property of
 * it per property, and the model of the type a property holds as the value of
 * that property.
 */
public final class FormModelWriter {
    /**
     * The suffix both the file and the class of a model go by, on top of the
     * name of the type the model is of.
     */
    public static final String SUFFIX = "Model";

    private static final String LIT_FORM = "@vaadin/hilla-lit-form";

    private static final String BEAN = """
            class {{name}}<T extends {{entity}} = {{entity}}> extends {{superModel}}<T> {
              static override createEmptyValue = {{emptyValue}}({{name}});

            {{properties}}
            }

            export default {{name}};""";

    /**
     * A type whose properties are all inherited, or which has none at all, so
     * that the model has nothing of its own to bind.
     */
    private static final String BEAN_WITHOUT_PROPERTIES = """
            class {{name}}<T extends {{entity}} = {{entity}}> extends {{superModel}}<T> {
              static override createEmptyValue = {{emptyValue}}({{name}});
            }

            export default {{name}};""";

    private static final String PROPERTY = """
            get {{property}}(): {{modelType}} {
              return this[{{getPropertyModel}}]('{{property}}', (parent, key) =>
                {{model}});
            }""";

    private static final String ENUMERATION = """
            class {{name}} extends {{enumModel}}<typeof {{entity}}> {
              static override createEmptyValue = {{emptyValue}}({{name}});

              readonly [{{enumKey}}] = {{entity}};
            }

            export default {{name}};""";

    public GeneratedFile write(EntityModel entity) {
        var path = ModulePaths.fileOf(entity.javaClass());

        return new GeneratedFile(
                path.substring(0, path.length() - ".ts".length()) + SUFFIX
                        + ".ts",
                switch (entity) {
                case EntityModel.Bean bean -> write(bean);
                case EntityModel.Enumeration enumeration -> write(enumeration);
                });
    }

    private static String write(EntityModel.Bean bean) {
        var name = ModulePaths.entityName(bean.javaClass()) + SUFFIX;
        var imports = new ImportRegistry();
        imports.reserve(name);

        var directory = ModulePaths.directoryOf(bean.javaClass());
        var types = new TypeWriter(imports, directory);
        var models = new ModelWriter(imports, types, directory);

        var body = Template
                .of(bean.properties().isEmpty() ? BEAN_WITHOUT_PROPERTIES
                        : BEAN) //
                .with("name", name) //
                .with("entity",
                        types.writeRequired(
                                TypeModel.EntityRef.of(bean.javaClass()))) //
                .with("superModel", superModel(bean, models)) //
                .with("emptyValue",
                        imports.importNamed(LIT_FORM,
                                "makeObjectEmptyValueCreator", false)) //
                .with("properties", writeProperties(bean, imports, models)) //
                .fill();

        return file(imports, body);
    }

    private static String write(EntityModel.Enumeration enumeration) {
        var name = ModulePaths.entityName(enumeration.javaClass()) + SUFFIX;
        var imports = new ImportRegistry();
        imports.reserve(name);

        // The enum itself is used as a value rather than as a type, since the
        // model tells the form which constants there are
        var entity = imports
                .importDefault(
                        ModulePaths.forEntity(enumeration.javaClass(),
                                ModulePaths
                                        .directoryOf(enumeration.javaClass())),
                        ModulePaths.entityName(enumeration.javaClass()), false);

        var body = Template.of(ENUMERATION) //
                .with("name", name) //
                .with("entity", entity) //
                .with("enumModel",
                        imports.importNamed(LIT_FORM, "EnumModel", false)) //
                .with("emptyValue",
                        imports.importNamed(LIT_FORM,
                                "makeEnumEmptyValueCreator", false)) //
                .with("enumKey", imports.importNamed(LIT_FORM, "_enum", false)) //
                .fill();

        return file(imports, body);
    }

    /**
     * The model the one being written extends, which is the model of the type
     * holding the inherited properties, or the model every other one is built
     * on.
     */
    private static String superModel(EntityModel.Bean bean,
            ModelWriter models) {
        return bean.superTypes().stream().findFirst().map(models::name)
                .orElseGet(models::objectModel);
    }

    private static String writeProperties(EntityModel.Bean bean,
            ImportRegistry imports, ModelWriter models) {
        var getPropertyModel = imports.importNamed(LIT_FORM,
                "_getPropertyModel", false);

        return bean.properties().stream()
                .map(property -> writeProperty(property, getPropertyModel,
                        models))
                .map(FormModelWriter::indent)
                .collect(Collectors.joining("\n\n"));
    }

    private static String writeProperty(PropertyModel property,
            String getPropertyModel, ModelWriter models) {
        return Template.of(PROPERTY) //
                .with("property", property.name()) //
                .with("modelType", models.type(property.type())) //
                .with("getPropertyModel", getPropertyModel) //
                .with("model", models.instance(property.type())) //
                .fill();
    }

    /**
     * Indents a block of lines by one step, since a template only says where
     * the block goes rather than how deep.
     */
    private static String indent(String block) {
        return block.lines().map(line -> "  " + line)
                .collect(Collectors.joining("\n"));
    }

    private static String file(ImportRegistry imports, String body) {
        var lines = new ArrayList<>(imports.write());
        lines.add("");
        lines.add(body);

        return String.join("\n", lines) + "\n";
    }
}
