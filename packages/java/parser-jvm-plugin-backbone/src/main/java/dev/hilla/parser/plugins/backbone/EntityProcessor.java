package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Arrays;
import java.util.stream.Collectors;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class EntityProcessor {
    private final Context context;

    EntityProcessor(Context context) {
        this.context = context;
    }

    public void process(ClassInfoModel entity) {
        var components = getComponents();

        var schema = entity.isEnum() ? processEnum(entity)
                : processExtendedClass(entity);

        components.addSchemas(entity.getName(), schema);

        context.getAssociationMap().addEntity(entity, schema);
    }

    private Components getComponents() {
        var openAPI = context.getOpenAPI();
        var components = openAPI.getComponents();

        if (components == null) {
            components = new Components();
            openAPI.setComponents(components);
        }

        return components;
    }

    private Schema<?> processClass(ClassInfoModel entity) {
        var schema = new ObjectSchema();

        entity.getFieldsStream().filter(field -> !field.isTransient())
                .forEach(field -> {
                    var fieldSchema = new SchemaProcessor(field.getType(),
                            context).process();
                    schema.addProperties(field.getName(), fieldSchema);
                    context.getAssociationMap().addField(field, fieldSchema);
                });

        return schema;
    }

    private Schema<?> processEnum(ClassInfoModel entity) {
        var schema = new StringSchema();

        schema.setEnum(entity.getFieldsStream().filter(FieldInfoModel::isPublic)
                .map(FieldInfoModel::getName).collect(Collectors.toList()));

        return schema;
    }

    private Schema<?> processExtendedClass(ClassInfoModel entity) {
        var processed = processClass(entity);

        return entity.getSuperClass()
                .filter(ClassRefSignatureModel::isNonJDKClass)
                .<Schema<?>> map(
                        cls -> new ComposedSchema().anyOf(Arrays.asList(
                                new Schema<>().$ref(
                                        COMPONENTS_SCHEMAS_REF + cls.getName()),
                                processed)))
                .orElse(processed);
    }
}
