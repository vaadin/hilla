package com.vaadin.fusion.parser.plugins.backbone;

import java.util.List;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.RelativeMethodInfo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

class EntityProcessor extends Processor {
    private static String decapitalize(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    public EntityProcessor(@Nonnull List<RelativeClassInfo> classes,
            @Nonnull OpenAPI model) {
        super(classes, model);
    }

    @Override
    public void process() {
        model.components(prepareComponents());
    }

    private Components prepareComponents() {
        Components components = new Components();

        for (RelativeClassInfo entity : classes) {
            ComponentSchemaProcessor processor = new ComponentSchemaProcessor(
                    entity);

            components.addSchemas(processor.getKey(), processor.getValue());
        }

        return components;
    }

    private static class ComponentSchemaProcessor {
        private final RelativeClassInfo entity;

        public ComponentSchemaProcessor(RelativeClassInfo entity) {
            this.entity = entity;
        }

        public String getKey() {
            return entity.get().getName();
        }

        public Schema<?> getValue() {
            ObjectSchema schema = new ObjectSchema();

            entity.getMethods().stream()
                    .filter(method -> method.get().getName().startsWith("get"))
                    .forEach(method -> {
                        ComponentSchemaPropertyProcessor processor = new ComponentSchemaPropertyProcessor(
                                method);

                        schema.addProperties(processor.getKey(),
                                processor.getValue());
                    });

            return schema;
        }
    }

    private static class ComponentSchemaPropertyProcessor {
        private final RelativeMethodInfo method;

        public ComponentSchemaPropertyProcessor(RelativeMethodInfo method) {
            this.method = method;
        }

        public String getKey() {
            return decapitalize(method.get().getName().substring(3));
        }

        public Schema<?> getValue() {
            return new SchemaProcessor(method.getResultType()).process();
        }
    }
}
