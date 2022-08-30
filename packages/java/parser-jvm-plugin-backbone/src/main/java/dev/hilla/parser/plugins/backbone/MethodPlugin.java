package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.ScanItem;
import dev.hilla.parser.core.ScanLocation;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.SignatureInfo;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.utils.Streams;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfoList;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public final class MethodPlugin
    implements Plugin, Plugin.ScanProcessor, Plugin.Scanner {
    static public final String METHOD_KIND = "method";
    private int order = 10;
    private SharedStorage storage;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void process(@Nonnull ScanLocation location) {
        if (location.getCurrent().isPresent() &&
            METHOD_KIND.equals(location.getCurrent().get().getKind())) {
            MethodInfoModel method =
                (MethodInfoModel) location.getContext().get(0).getModel();
            ClassInfoModel endpoint =
                (ClassInfoModel) location.getContext().get(1).getModel();

            var methodName = method.getName();
            var endpointName = endpoint.getSimpleName();
            var pathKey = "/" + endpointName + "/" + methodName;

            storage.getOpenAPI()
                .path(pathKey,
                    new PathItem().post(createOperation(endpoint, method)));
        }
    }

    private Operation createOperation(ClassInfoModel endpoint,
        MethodInfoModel method) {
        var operation = new Operation();
        var endpointName = endpoint.getSimpleName();
        operation.operationId(endpointName + '_' + method.getName() + "_POST")
            .addTagsItem(endpointName).responses(createResponses(endpoint,
                method));

        if (!method.getParameters().isEmpty()) {
            operation.requestBody(createRequestBody(endpoint, method));
        }

        return operation;
    }

    private RequestBody createRequestBody(ClassInfoModel endpoint,
        MethodInfoModel method) {
        var requestMap = new ObjectSchema();
        for (var parameter : method.getParameters()) {
            var schema = new SchemaProcessor(parameter.getType(),
                new SignatureInfo(parameter), storage).process();
            requestMap.addProperties(parameter.getName(), schema);
            storage.getAssociationMap().addParameter(schema, parameter);
        }
        return new RequestBody().content(
            new Content().addMediaType("application/json",
                new MediaType().schema(requestMap)));
    }

    private ApiResponses createResponses(ClassInfoModel endpoint,
        MethodInfoModel method) {
        var response = new ApiResponse().description("");
        var resultType = method.getResultType();

        if (!resultType.isVoid()) {
            var schema = new SchemaProcessor(resultType,
                new SignatureInfo(method), storage).process();

            response.setContent(new Content().addMediaType("application/json",
                new MediaType().schema(schema)));

            storage.getAssociationMap().addMethod(schema, method);
        }

        return new ApiResponses().addApiResponse("200", response);
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    @Override
    public ScanLocation scan(@Nonnull ScanLocation location) {
        if (location.getCurrent().isPresent() &&
            EndpointPlugin.ENDPOINT_KIND.equals(
                location.getCurrent().get().getKind())) {
            final String endpointAnnotationName = storage.getEndpointAnnotationName();
            final String endpointExposedAnnotationName = storage.getEndpointExposedAnnotationName();
            final ClassInfoModel cls = (ClassInfoModel) location.getCurrent()
                .get().getModel();
            Stream<ScanItem> methods = ((ClassInfo) cls.get()).getMethodInfo()
                .stream().filter(methodInfo -> !methodInfo.isPrivate() && !methodInfo.isProtected()).filter(
                    methodInfo -> ClassInfoModel.isNonJDKClass(
                        methodInfo.getClassInfo())).filter(methodInfo ->
                    methodInfo.getClassInfo()
                        .hasAnnotation(endpointAnnotationName) ||
                        methodInfo.getClassInfo()
                            .hasAnnotation(endpointExposedAnnotationName))
                .map(MethodInfoModel::of)
                .map(method -> new ScanItem(method, METHOD_KIND));
            return new ScanLocation(location.getContext(),
                Streams.combine(location.getNext().stream(), methods)
                    .collect(Collectors.toList()));
        }
        return location;
    }
}
