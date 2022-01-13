package com.vaadin.fusion.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.ReflectedClass;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.RelativeFieldInfo;

import io.github.classgraph.FieldInfo;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class EntityProcessor extends Processor {
    public EntityProcessor(@Nonnull Collection<RelativeClassInfo> classes,
            @Nonnull OpenAPI model, @Nonnull AssociationMap associationMap) {
        super(classes, model, associationMap);
    }

    private static String decapitalize(String string) {
        var c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    @Override
    public void process() {
        model.components(prepareComponents());
    }

    private Components prepareComponents() {
        var components = new Components();

        classes.stream().filter(cls -> {
            var reflectedClass = new ReflectedClass(cls);

            return !reflectedClass.isDate() && !reflectedClass.isDateTime()
                    && !reflectedClass.isIterable() && !reflectedClass.isMap();
        }).flatMap(cls -> cls.getInheritanceChain().getClassesStream())
                .forEach(entity -> {
                    if (components.getSchemas() == null
                            || (!components.getSchemas()
                                    .containsKey(entity.get().getName()))) {
                        var processor = new ComponentSchemaProcessor(entity);

                        components.addSchemas(processor.getKey(),
                                processor.getValue());

                        associationMap.addEntity(processor.getValue(), entity);
                    }
                });

        return components;
    }

    private class ComponentSchemaProcessor {
        private final RelativeClassInfo entity;
        private final String key;
        private final Schema<?> value;

        public ComponentSchemaProcessor(RelativeClassInfo entity) {
            this.entity = entity;

            var info = entity.get();
            this.key = info.getName();
            this.value = info.isEnum() ? processEnum() : processExtendedClass();
        }

        public String getKey() {
            return key;
        }

        public Schema<?> getValue() {
            return value;
        }

        private Schema<?> processClass() {
            var schema = new ObjectSchema();

            entity.getFieldsStream().forEach(field -> {
                var processor = new ComponentSchemaPropertyProcessor(field);

                schema.addProperties(processor.getKey(), processor.getValue());

                associationMap.addField(processor.getValue(), field);
            });

            return schema;
        }

        private Schema<?> processEnum() {
            var schema = new StringSchema();

            schema.setEnum(entity.getFieldsStream().map(RelativeFieldInfo::get)
                    .filter(FieldInfo::isPublic).map(FieldInfo::getName)
                    .collect(Collectors.toList()));

            return schema;
        }

        private Schema<?> processExtendedClass() {
            var processed = processClass();

            return entity.getSuperClass()
                    .<Schema<?>> map(cls -> new ComposedSchema().anyOf(Arrays
                            .asList(new Schema<>().$ref(COMPONENTS_SCHEMAS_REF
                                    + cls.get().getName()), processed)))
                    .orElse(processed);
        }
    }

    private class ComponentSchemaPropertyProcessor {
        private final String key;
        private final Schema<?> value;

        public ComponentSchemaPropertyProcessor(RelativeFieldInfo field) {
            this.key = field.get().getName();
            this.value = new SchemaProcessor(field.getType(), associationMap)
                    .process();
        }

        public String getKey() {
            return key;
        }

        public Schema<?> getValue() {
            return value;
        }
    }
}
