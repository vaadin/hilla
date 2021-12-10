package com.vaadin.fusion.parser.plugins.backbone;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.RelativeMethodInfo;

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

final class EndpointProcessor extends Processor {
    public EndpointProcessor(@Nonnull Collection<RelativeClassInfo> classes,
            @Nonnull OpenAPI model) {
        super(classes, model);
    }

    @Override
    public void process() {
        model.tags(prepareTags()).paths(preparePaths());
    }

    private Paths preparePaths() {
        return classes.stream()
                .flatMap(cls -> cls.getInheritanceChain().getMethodsStream())
                .filter(method -> method.get().isPublic())
                .map(MethodProcessor::new)
                .collect(Collectors.toMap(MethodProcessor::getPathKey,
                        MethodProcessor::getPathItem, (o1, o2) -> o1,
                        Paths::new));
    }

    private List<Tag> prepareTags() {
        return classes.stream()
                .map(cls -> new Tag().name(cls.get().getSimpleName()))
                .collect(Collectors.toList());
    }

    private static class MethodProcessor {
        private final RelativeMethodInfo method;

        public MethodProcessor(RelativeMethodInfo method) {
            this.method = method;
        }

        public PathItem getPathItem() {
            return new PathItem().post(createOperation());
        }

        public String getPathKey() {
            var endpointName = method.getParent()
                    .map(cls -> cls.get().getSimpleName()).orElse("Unknown");
            var methodName = method.get().getName();

            return "/" + endpointName + "/" + methodName;
        }

        private Operation createOperation() {
            var operation = new Operation();

            var endpointName = method.getParent()
                    .map(cls -> cls.get().getSimpleName()).orElse("Unknown");

            operation
                    .operationId(endpointName + '_' + method.get().getName()
                            + "_POST")
                    .addTagsItem(endpointName).responses(createResponses());

            if (method.getParameters().size() > 0) {
                operation.requestBody(createRequestBody());
            }

            return operation;
        }

        private RequestBody createRequestBody() {
            var requestMap = new ObjectSchema();

            method.getParameters().forEach(parameter -> {
                requestMap.addProperties(parameter.get().getName(),
                        new SchemaProcessor(parameter.getType()).process());
            });

            return new RequestBody().content(new Content().addMediaType(
                    "application/json", new MediaType().schema(requestMap)));
        }

        private ApiResponses createResponses() {
            var content = new Content();

            var resultType = method.getResultType();

            if (!resultType.isVoid()) {
                content.addMediaType("application/json", new MediaType()
                        .schema(new SchemaProcessor(resultType).process()));
            }

            return new ApiResponses().addApiResponse("200",
                    new ApiResponse().content(content).description(""));
        }
    }
}
