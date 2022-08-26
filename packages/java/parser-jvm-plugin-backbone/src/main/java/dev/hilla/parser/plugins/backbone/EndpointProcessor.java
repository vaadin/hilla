package dev.hilla.parser.plugins.backbone;

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

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

final class EndpointProcessor {
    private final Context context;

    EndpointProcessor(Context context) {
        this.context = context;
    }

    public void process(ClassInfoModel endpoint) {
        var paths = getPaths();
        var associationMap = context.getAssociationMap();

        endpoint.getInheritanceChainStream()
            .flatMap(ClassInfoModel::getMethodsStream)
            .filter(MethodInfoModel::isPublic)
            .map(method -> new MethodProcessor(endpoint, method,
                context))
            .forEach(processor -> paths.addPathItem(
                processor.createPathKey(),
                processor.createPathItem()));

        context.getOpenAPI()
            .addTagsItem(new Tag().name(endpoint.getSimpleName()));
    }

    private Paths getPaths() {
        var openAPI = context.getOpenAPI();
        var paths = openAPI.getPaths();

        if (paths == null) {
            paths = new Paths();
            openAPI.setPaths(paths);
        }

        return paths;
    }

    static final class MethodProcessor {
        private final Context context;
        private final ClassInfoModel endpoint;
        private final MethodInfoModel method;

        MethodProcessor(ClassInfoModel endpoint, MethodInfoModel method,
                Context context) {
            this.context = context;
            this.endpoint = endpoint;
            this.method = method;
        }

        PathItem createPathItem() {
            return new PathItem().post(createOperation(endpoint, method));
        }

        String createPathKey() {
            return "/" + endpoint.getSimpleName() + "/" + method.getName();
        }

        private Operation createOperation(ClassInfoModel endpoint,
                MethodInfoModel method) {
            var operation = new Operation();

            var endpointName = endpoint.getSimpleName();

            operation
                    .operationId(
                            endpointName + '_' + method.getName() + "_POST")
                    .addTagsItem(endpointName)
                    .responses(createResponses(method));

            if (method.getParameters().size() > 0) {
                operation.requestBody(createRequestBody(method));
            }

            return operation;
        }

        private RequestBody createRequestBody(MethodInfoModel method) {
            var requestMap = new ObjectSchema();

            for (var parameter : method.getParameters()) {
                var schema = new SchemaProcessor(parameter.getType(), context)
                        .process();
                requestMap.addProperties(parameter.getName(), schema);
                context.getAssociationMap().addParameter(parameter, schema);
            }

            return new RequestBody().content(new Content().addMediaType(
                    "application/json", new MediaType().schema(requestMap)));
        }

        private ApiResponses createResponses(MethodInfoModel method) {
            var response = new ApiResponse().description("");

            var resultType = method.getResultType();

            if (!resultType.isVoid()) {
                var schema = new SchemaProcessor(resultType, context).process();

                response.setContent(new Content().addMediaType(
                        "application/json", new MediaType().schema(schema)));

                context.getAssociationMap().addMethod(method, schema);
            }

            return new ApiResponses().addApiResponse("200", response);
        }
    }
}
