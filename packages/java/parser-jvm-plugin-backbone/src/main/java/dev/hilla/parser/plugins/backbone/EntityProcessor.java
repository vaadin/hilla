package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.ReflectedClass;
import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.RelativeFieldInfo;

import io.github.classgraph.FieldInfo;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class EntityProcessor {
    private final Collection<RelativeClassInfo> classes;
    private final Context context;
    private final OpenAPI model;

    public EntityProcessor(@Nonnull Collection<RelativeClassInfo> classes,
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

        classes.stream().filter(cls -> !cls.get().isSynthetic())
                .filter(cls -> context.getRefs().contains(cls.get().getName()))
                .filter(cls -> {
                    var reflectedClass = new ReflectedClass(cls);

                    return !reflectedClass.isDate()
                            && !reflectedClass.isDateTime()
                            && !reflectedClass.isIterable()
                            && !reflectedClass.isMap();
                }).flatMap(cls -> cls.getInheritanceChain().getClassesStream())
                .distinct().forEach(entity -> {
                    if (components.getSchemas() == null
                            || (!components.getSchemas()
                                    .containsKey(entity.get().getName()))) {
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

                context.getAssociationMap().addField(processor.getValue(),
                        field);
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

            return entity.getSuperClass().<Schema<?>> map(cls -> {
                var fullyQualifiedClassName = cls.get().getName();

                context.getRefs().add(fullyQualifiedClassName);

                return new ComposedSchema().anyOf(Arrays.asList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedClassName),
                        processed));
            }).orElse(processed);
        }
    }

    private class ComponentSchemaPropertyProcessor {
        private final String key;
        private final Schema<?> value;

        public ComponentSchemaPropertyProcessor(RelativeFieldInfo field) {
            this.key = field.get().getName();
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
