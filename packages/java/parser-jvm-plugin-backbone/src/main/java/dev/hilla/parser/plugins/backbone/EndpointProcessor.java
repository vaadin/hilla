package dev.hilla.parser.plugins.backbone;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.SignatureInfo;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;

final class EndpointProcessor {
    private final Collection<ClassInfoModel> classes;
    private final OpenAPI model;
    private final SharedStorage storage;

    public EndpointProcessor(@Nonnull Collection<ClassInfoModel> classes,
            @Nonnull OpenAPI model, @Nonnull SharedStorage storage) {
        this.classes = Objects.requireNonNull(classes);
        this.storage = Objects.requireNonNull(storage);
        this.model = Objects.requireNonNull(model);
    }

    public void process() {
        model.tags(prepareTags()).paths(preparePaths());
    }

    private Paths preparePaths() {
        return classes.stream()
                .flatMap(cls -> cls.getInheritanceChainStream()
                        .flatMap(ClassInfoModel::getMethodsStream))
                .filter(MethodInfoModel::isPublic).map(MethodProcessor::new)
                .collect(Collectors.toMap(MethodProcessor::getPathKey,
                        MethodProcessor::getPathItem, (o1, o2) -> o1,
                        Paths::new));
    }

    private List<Tag> prepareTags() {
        return classes.stream().map(cls -> new Tag().name(cls.getSimpleName()))
                .collect(Collectors.toList());
    }

    private class MethodProcessor {
        private final MethodInfoModel method;
        private final PathItem pathItem;
        private final String pathKey;

        public MethodProcessor(MethodInfoModel method) {
            this.method = method;
            this.pathItem = new PathItem().post(createOperation());

            var endpointName = method.getOwner().getSimpleName();
            var methodName = method.getName();

            this.pathKey = "/" + endpointName + "/" + methodName;
        }

        public PathItem getPathItem() {
            return pathItem;
        }

        public String getPathKey() {
            return pathKey;
        }

        private Operation createOperation() {
            var operation = new Operation();

            var endpointName = method.getOwner().getSimpleName();

            operation
                    .operationId(
                            endpointName + '_' + method.getName() + "_POST")
                    .addTagsItem(endpointName).responses(createResponses());

            if (method.getParameters().size() > 0) {
                operation.requestBody(createRequestBody());
            }

            return operation;
        }

        private RequestBody createRequestBody() {
            var requestMap = new ObjectSchema();

            for (var parameter : method.getParameters()) {
                var schema = new SchemaProcessor(parameter.getType(),
                        new SignatureInfo(parameter), storage).process();
                requestMap.addProperties(parameter.getName(), schema);
                storage.getAssociationMap().addParameter(schema, parameter);
            }

            return new RequestBody().content(new Content().addMediaType(
                    "application/json", new MediaType().schema(requestMap)));
        }

        private ApiResponses createResponses() {
            var content = new Content();

            var resultType = method.getResultType();

            if (!resultType.isVoid()) {
                var schema = new SchemaProcessor(resultType,
                        new SignatureInfo(method), storage).process();

                content.addMediaType("application/json",
                        new MediaType().schema(schema));
                storage.getAssociationMap().addMethod(schema, method);
            }

            return new ApiResponses().addApiResponse("200",
                    new ApiResponse().content(content).description(""));
        }
    }
}
