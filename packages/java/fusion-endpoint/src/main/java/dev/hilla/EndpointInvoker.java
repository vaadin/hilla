package dev.hilla;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import dev.hilla.EndpointRegistry.VaadinEndpointData;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.endpointransfermapper.EndpointTransferMapper;
import dev.hilla.exception.EndpointException;
import dev.hilla.exception.EndpointValidationException;
import dev.hilla.exception.EndpointValidationException.ValidationErrorData;

/**
 * Handles invocation of endpoint methods after checking the user has proper
 * access.
 * <p>
 * This class is a generic invoker that does not have knowledge of HTTP requests
 * or the context that the method is being invoked in.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Component
public class EndpointInvoker {

    private final ObjectMapper vaadinEndpointMapper;
    private final Validator validator = Validation
            .buildDefaultValidatorFactory().getValidator();
    private final ExplicitNullableTypeChecker explicitNullableTypeChecker;
    private final ApplicationContext applicationContext;

    EndpointRegistry endpointRegistry;

    private static EndpointTransferMapper endpointTransferMapper = new EndpointTransferMapper();

    /**
     * Creates an instance of this bean.
     * 
     * @param applicationContext
     *            Spring context to extract beans annotated with
     *            {@link Endpoint} from
     * @param vaadinEndpointMapper
     *            optional bean to override the default {@link ObjectMapper}
     *            that is used for serializing and deserializing request and
     *            response bodies Use
     *            {@link EndpointController#VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER}
     *            qualifier to override the mapper.
     * @param explicitNullableTypeChecker
     *            the method parameter and return value type checker to verify
     *            that null values are explicit
     * @param endpointRegistry
     *            the registry used to store endpoint information
     */
    public EndpointInvoker(ApplicationContext applicationContext,
            ObjectMapper vaadinEndpointMapper,
            ExplicitNullableTypeChecker explicitNullableTypeChecker,
            EndpointRegistry endpointRegistry) {
        this.applicationContext = applicationContext;
        this.vaadinEndpointMapper = vaadinEndpointMapper != null
                ? vaadinEndpointMapper
                : createVaadinConnectObjectMapper(applicationContext);
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        this.endpointRegistry = endpointRegistry;

    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(EndpointInvoker.class);
    }

    /**
     * Invoke the given endpoint method with the given parameters if the user
     * has access to do so.
     * 
     * @param endpointName
     *            the name of the endpoint
     * @param methodName
     *            the name of the method in the endpoint
     * @param body
     *            optional request body, that should be specified if the method
     *            called has parameters
     * @param request
     *            the HTTP request which should not be here in the end
     * @return the return value of the invoked endpoint method, wrapped in a
     *         response entity
     */
    public ResponseEntity<String> invoke(String endpointName, String methodName,
            ObjectNode body, HttpServletRequest request) {
        VaadinEndpointData vaadinEndpointData = endpointRegistry
                .get(endpointName);
        if (vaadinEndpointData == null) {
            getLogger().debug("Endpoint '{}' not found", endpointName);
            return ResponseEntity.notFound().build();
        }

        Method methodToInvoke = vaadinEndpointData.getMethod(methodName)
                .orElse(null);
        if (methodToInvoke == null) {
            getLogger().debug("Method '{}' not found in endpoint '{}'",
                    methodName, endpointName);
            return ResponseEntity.notFound().build();
        }

        try {

            // Put a VaadinRequest in the instances object so as the request is
            // available in the end-point method
            VaadinServletService service = (VaadinServletService) VaadinService
                    .getCurrent();
            CurrentInstance.set(VaadinRequest.class,
                    new VaadinServletRequest(request, service));
            return invokeVaadinEndpointMethod(endpointName, methodName,
                    methodToInvoke, body, vaadinEndpointData, request);
        } catch (JsonProcessingException e) {
            String errorMessage = String.format(
                    "Failed to serialize endpoint '%s' method '%s' response. "
                            + "Double check method's return type or specify a custom mapper bean with qualifier '%s'",
                    endpointName, methodName,
                    EndpointController.VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER);
            getLogger().error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseErrorObject(errorMessage));
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }

    }

    private ObjectMapper createVaadinConnectObjectMapper(
            ApplicationContext context) {
        Jackson2ObjectMapperBuilder builder = context
                .getBean(Jackson2ObjectMapperBuilder.class);
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        JacksonProperties jacksonProperties = context
                .getBean(JacksonProperties.class);
        if (jacksonProperties.getVisibility().isEmpty()) {
            objectMapper.setVisibility(PropertyAccessor.ALL,
                    JsonAutoDetect.Visibility.ANY);
        }
        return objectMapper;
    }

    private ResponseEntity<String> invokeVaadinEndpointMethod(
            String endpointName, String methodName, Method methodToInvoke,
            ObjectNode body, VaadinEndpointData vaadinEndpointData,
            HttpServletRequest request) throws JsonProcessingException {
        EndpointAccessChecker accessChecker = getAccessChecker(
                request.getServletContext());
        String checkError = accessChecker.check(methodToInvoke, request);
        if (checkError != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createResponseErrorObject(String.format(
                            "Endpoint '%s' method '%s' request cannot be accessed, reason: '%s'",
                            endpointName, methodName, checkError)));
        }

        Map<String, JsonNode> requestParameters = getRequestParameters(body);
        Type[] javaParameters = getJavaParameters(methodToInvoke, ClassUtils
                .getUserClass(vaadinEndpointData.getEndpointObject()));
        if (javaParameters.length != requestParameters.size()) {
            return ResponseEntity.badRequest()
                    .body(createResponseErrorObject(String.format(
                            "Incorrect number of parameters for endpoint '%s' method '%s', "
                                    + "expected: %s, got: %s",
                            endpointName, methodName, javaParameters.length,
                            requestParameters.size())));
        }

        Object[] vaadinEndpointParameters;
        try {
            vaadinEndpointParameters = getVaadinEndpointParameters(
                    requestParameters, javaParameters, methodName,
                    endpointName);
        } catch (EndpointValidationException e) {
            getLogger().debug(
                    "Endpoint '{}' method '{}' received invalid response",
                    endpointName, methodName, e);
            return ResponseEntity.badRequest().body(vaadinEndpointMapper
                    .writeValueAsString(e.getSerializationData()));
        }

        Set<ConstraintViolation<Object>> methodParameterConstraintViolations = validator
                .forExecutables()
                .validateParameters(vaadinEndpointData.getEndpointObject(),
                        methodToInvoke, vaadinEndpointParameters);
        if (!methodParameterConstraintViolations.isEmpty()) {
            return ResponseEntity.badRequest().body(vaadinEndpointMapper
                    .writeValueAsString(new EndpointValidationException(
                            String.format(
                                    "Validation error in endpoint '%s' method '%s'",
                                    endpointName, methodName),
                            createMethodValidationErrors(
                                    methodParameterConstraintViolations))
                                            .getSerializationData()));
        }

        Object returnValue;
        try {
            returnValue = methodToInvoke.invoke(
                    vaadinEndpointData.getEndpointObject(),
                    vaadinEndpointParameters);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format(
                    "Received incorrect arguments for endpoint '%s' method '%s'. "
                            + "Expected parameter types (and their order) are: '[%s]'",
                    endpointName, methodName,
                    listMethodParameterTypes(javaParameters));
            getLogger().debug(errorMessage, e);
            return ResponseEntity.badRequest()
                    .body(createResponseErrorObject(errorMessage));
        } catch (IllegalAccessException e) {
            String errorMessage = String.format(
                    "Endpoint '%s' method '%s' access failure", endpointName,
                    methodName);
            getLogger().error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseErrorObject(errorMessage));
        } catch (InvocationTargetException e) {
            return handleMethodExecutionError(endpointName, methodName, e);
        }

        returnValue = endpointTransferMapper.toTransferType(returnValue);

        String implicitNullError = this.explicitNullableTypeChecker
                .checkValueForAnnotatedElement(returnValue, methodToInvoke);
        if (implicitNullError != null) {
            EndpointException returnValueException = new EndpointException(
                    String.format(
                            "Unexpected return value in endpoint '%s' method '%s'. %s",
                            endpointName, methodName, implicitNullError));

            getLogger().error(returnValueException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(vaadinEndpointMapper.writeValueAsString(
                            returnValueException.getSerializationData()));
        }

        Set<ConstraintViolation<Object>> returnValueConstraintViolations = validator
                .forExecutables()
                .validateReturnValue(vaadinEndpointData.getEndpointObject(),
                        methodToInvoke, returnValue);
        if (!returnValueConstraintViolations.isEmpty()) {
            getLogger().error(
                    "Endpoint '{}' method '{}' had returned a value that has validation errors: '{}', this might cause bugs on the client side. Fix the method implementation.",
                    endpointName, methodName, returnValueConstraintViolations);
        }
        return ResponseEntity
                .ok(vaadinEndpointMapper.writeValueAsString(returnValue));
    }

    private Type[] getJavaParameters(Method methodToInvoke, Type classType) {
        return Stream.of(GenericTypeReflector
                .getExactParameterTypes(methodToInvoke, classType))
                .toArray(Type[]::new);
    }

    private ResponseEntity<String> handleMethodExecutionError(
            String endpointName, String methodName, InvocationTargetException e)
            throws JsonProcessingException {
        if (EndpointException.class.isAssignableFrom(e.getCause().getClass())) {
            EndpointException endpointException = ((EndpointException) e
                    .getCause());
            getLogger().debug("Endpoint '{}' method '{}' aborted the execution",
                    endpointName, methodName, endpointException);
            return ResponseEntity.badRequest()
                    .body(vaadinEndpointMapper.writeValueAsString(
                            endpointException.getSerializationData()));
        } else {
            String errorMessage = String.format(
                    "Endpoint '%s' method '%s' execution failure", endpointName,
                    methodName);
            getLogger().error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseErrorObject(errorMessage));
        }
    }

    String createResponseErrorObject(String errorMessage) {
        ObjectNode objectNode = vaadinEndpointMapper.createObjectNode();
        objectNode.put(EndpointException.ERROR_MESSAGE_FIELD, errorMessage);
        return objectNode.toString();
    }

    private String listMethodParameterTypes(Type[] javaParameters) {
        return Stream.of(javaParameters).map(Type::getTypeName)
                .collect(Collectors.joining(", "));
    }

    private Object[] getVaadinEndpointParameters(
            Map<String, JsonNode> requestParameters, Type[] javaParameters,
            String methodName, String endpointName) {
        Object[] endpointParameters = new Object[javaParameters.length];
        String[] parameterNames = new String[requestParameters.size()];
        requestParameters.keySet().toArray(parameterNames);
        Map<String, String> errorParams = new HashMap<>();
        Set<ConstraintViolation<Object>> constraintViolations = new LinkedHashSet<>();

        for (int i = 0; i < javaParameters.length; i++) {
            Type parameterType = javaParameters[i];
            Type incomingType = parameterType;
            try {
                Class<?> mappedType = getTransferType(parameterType);
                if (mappedType != null) {
                    incomingType = mappedType;
                }
                Object parameter = vaadinEndpointMapper
                        .readerFor(vaadinEndpointMapper.getTypeFactory()
                                .constructType(incomingType))
                        .readValue(requestParameters.get(parameterNames[i]));
                if (mappedType != null) {
                    parameter = endpointTransferMapper.toEndpointType(parameter,
                            (Class) parameterType);
                }
                endpointParameters[i] = parameter;

                if (parameter != null) {
                    constraintViolations.addAll(validator.validate(parameter));
                }
            } catch (IOException e) {
                String typeName = parameterType.getTypeName();
                getLogger().error(
                        "Unable to deserialize an endpoint '{}' method '{}' "
                                + "parameter '{}' with type '{}'",
                        endpointName, methodName, parameterNames[i], typeName,
                        e);
                errorParams.put(parameterNames[i], typeName);
            }
        }

        if (errorParams.isEmpty() && constraintViolations.isEmpty()) {
            return endpointParameters;
        }
        throw getInvalidEndpointParametersException(methodName, endpointName,
                errorParams, constraintViolations);
    }

    private Class<?> getTransferType(Type type) {
        if (!(type instanceof Class)) {
            return null;
        }

        return endpointTransferMapper.getTransferType((Class) type);
    }

    private EndpointValidationException getInvalidEndpointParametersException(
            String methodName, String endpointName,
            Map<String, String> deserializationErrors,
            Set<ConstraintViolation<Object>> constraintViolations) {
        List<ValidationErrorData> validationErrorData = new ArrayList<>(
                deserializationErrors.size() + constraintViolations.size());

        for (Map.Entry<String, String> deserializationError : deserializationErrors
                .entrySet()) {
            String message = String.format(
                    "Unable to deserialize an endpoint method parameter into type '%s'",
                    deserializationError.getValue());
            validationErrorData.add(new ValidationErrorData(message,
                    deserializationError.getKey()));
        }

        validationErrorData
                .addAll(createBeanValidationErrors(constraintViolations));

        String message = String.format(
                "Validation error in endpoint '%s' method '%s'", endpointName,
                methodName);
        return new EndpointValidationException(message, validationErrorData);
    }

    private List<ValidationErrorData> createBeanValidationErrors(
            Collection<ConstraintViolation<Object>> beanConstraintViolations) {
        return beanConstraintViolations.stream().map(
                constraintViolation -> new ValidationErrorData(String.format(
                        "Object of type '%s' has invalid property '%s' with value '%s', validation error: '%s'",
                        constraintViolation.getRootBeanClass(),
                        constraintViolation.getPropertyPath().toString(),
                        constraintViolation.getInvalidValue(),
                        constraintViolation.getMessage()),
                        constraintViolation.getPropertyPath().toString()))
                .collect(Collectors.toList());
    }

    private List<ValidationErrorData> createMethodValidationErrors(
            Collection<ConstraintViolation<Object>> methodConstraintViolations) {
        return methodConstraintViolations.stream().map(constraintViolation -> {
            String parameterPath = constraintViolation.getPropertyPath()
                    .toString();
            return new ValidationErrorData(String.format(
                    "Method '%s' of the object '%s' received invalid parameter '%s' with value '%s', validation error: '%s'",
                    parameterPath.split("\\.")[0],
                    constraintViolation.getRootBeanClass(), parameterPath,
                    constraintViolation.getInvalidValue(),
                    constraintViolation.getMessage()), parameterPath);
        }).collect(Collectors.toList());
    }

    private Map<String, JsonNode> getRequestParameters(ObjectNode body) {
        Map<String, JsonNode> parametersData = new LinkedHashMap<>();
        if (body != null) {
            body.fields().forEachRemaining(entry -> parametersData
                    .put(entry.getKey(), entry.getValue()));
        }
        return parametersData;
    }

    private static class VaadinConnectAccessCheckerWrapper {
        private final EndpointAccessChecker accessChecker;

        private VaadinConnectAccessCheckerWrapper(
                EndpointAccessChecker checker) {
            accessChecker = checker;
        }
    }

    EndpointAccessChecker getAccessChecker(ServletContext servletContext) {
        VaadinServletContext vaadinServletContext = new VaadinServletContext(
                servletContext);
        VaadinConnectAccessCheckerWrapper wrapper = vaadinServletContext
                .getAttribute(VaadinConnectAccessCheckerWrapper.class, () -> {
                    EndpointAccessChecker accessChecker = applicationContext
                            .getBean(EndpointAccessChecker.class);
                    return new VaadinConnectAccessCheckerWrapper(accessChecker);
                });
        return wrapper.accessChecker;
    }

}
