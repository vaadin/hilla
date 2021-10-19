package com.vaadin.fusion.parser.plugins.backbone;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.RelativeMethodInfo;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import io.github.classgraph.MethodTypeSignature;
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

class EndpointProcessor extends Processor {
    public EndpointProcessor(List<RelativeClassInfo> classes, OpenAPI model) {
        super(classes, model);
    }

    @Override
    public void process() {
        model.tags(prepareTags()).paths(preparePaths());
    }

    private List<Tag> prepareTags() {
        return classes.stream()
                .map(cls -> new Tag().name(cls.get().getSimpleName()))
                .collect(Collectors.toList());
    }

    private Paths preparePaths() {
        return classes.stream().flatMap(cls -> cls.getMethods().stream())
                .map(MethodProcessor::new)
                .collect(Collectors.toMap(MethodProcessor::getPathKey,
                        MethodProcessor::getPathItem, (o1, o2) -> o1,
                        Paths::new));
    }

    private static class MethodProcessor {
        private final RelativeMethodInfo method;

        public MethodProcessor(RelativeMethodInfo method) {
            this.method = method;
        }

        public String getPathKey() {
            String endpointName = method.getParent()
                    .map(cls -> cls.get().getSimpleName()).orElse("Unknown");
            String methodName = method.get().getName();

            return "/" + endpointName + "/" + methodName;
        }

        public PathItem getPathItem() {
            return new PathItem().post(createOperation());
        }

        private Operation createOperation() {
            Operation operation = new Operation();

            String endpointName = method.getParent()
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
            ObjectSchema requestMap = new ObjectSchema();

            method.getParameters().forEach(parameter -> {
                requestMap.addProperties(parameter.get().getName(),
                        new SchemaProcessor(parameter.getType()).process());
            });

            return new RequestBody().content(new Content().addMediaType(
                    "application/json", new MediaType().schema(requestMap)));
        }

        private ApiResponses createResponses() {
            Content content = new Content();

            RelativeTypeSignature resultType = method.getResultType();

            if (!resultType.isVoid()) {
                content.addMediaType("application/json", new MediaType()
                        .schema(new SchemaProcessor(resultType).process()));
            }

            return new ApiResponses().addApiResponse("200",
                    new ApiResponse().content(content).description(""));
        }
    }
}
