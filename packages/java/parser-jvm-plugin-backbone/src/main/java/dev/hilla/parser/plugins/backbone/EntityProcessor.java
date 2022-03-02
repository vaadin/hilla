package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class EntityProcessor {
    private final Collection<ClassInfoModel> classes;
    private final Context context;
    private final OpenAPI model;

    public EntityProcessor(@Nonnull Collection<ClassInfoModel> classes,
            @Nonnull OpenAPI model, @Nonnull Context context) {
        this.classes = Objects.requireNonNull(classes);
        this.context = Objects.requireNonNull(context);
        this.model = Objects.requireNonNull(model);
    }

    public void process() {
        model.components(prepareComponents());
    }

    private Components prepareComponents() {
        var components = new Components();

        classes.stream().filter(cls -> !cls.isSynthetic())
                .filter(cls -> !cls.isDate() && !cls.isDateTime()
                        && !cls.isIterable() && !cls.isMap())
                .flatMap(cls -> cls.getInheritanceChain().getClassesStream())
                .distinct().forEach(entity -> {
                    if (components.getSchemas() == null || (!components
                            .getSchemas().containsKey(entity.getName()))) {
                        var processor = new ComponentSchemaProcessor(entity);

                        components.addSchemas(processor.getKey(),
                                processor.getValue());

                        context.getAssociationMap()
                                .addEntity(processor.getValue(), entity);
                    }
                });

        return components;
    }

    private class ComponentSchemaProcessor {
        private final ClassInfoModel entity;
        private final String key;
        private final Schema<?> value;

        public ComponentSchemaProcessor(ClassInfoModel entity) {
            this.entity = entity;

            this.key = entity.getName();
            this.value = entity.isEnum() ? processEnum()
                    : processExtendedClass();
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

                context.getAssociationMap().addField(processor.getValue(),
                        field);
            });

            return schema;
        }

        private Schema<?> processEnum() {
            var schema = new StringSchema();

            schema.setEnum(entity.getFieldsStream()
                    .filter(FieldInfoModel::isPublic)
                    .map(FieldInfoModel::getName).collect(Collectors.toList()));

            return schema;
        }

        private Schema<?> processExtendedClass() {
            var processed = processClass();

            return entity.getSuperClass().<Schema<?>> map(
                    cls -> new ComposedSchema().anyOf(Arrays.asList(
                            new Schema<>().$ref(
                                    COMPONENTS_SCHEMAS_REF + cls.getName()),
                            processed)))
                    .orElse(processed);
        }
    }

    private class ComponentSchemaPropertyProcessor {
        private final String key;
        private final Schema<?> value;

        public ComponentSchemaPropertyProcessor(FieldInfoModel field) {
            this.key = field.getName();
            this.value = new SchemaProcessor(field.getType(), context)
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
