/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.server.connect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.vaadin.flow.server.connect.EndpointRegistry.VaadinEndpointData;
import com.vaadin.flow.server.connect.auth.VaadinConnectAccessChecker;
import com.vaadin.flow.server.connect.exception.EndpointException;
import com.vaadin.flow.server.connect.exception.EndpointValidationException;
import com.vaadin.flow.server.connect.exception.EndpointValidationException.ValidationErrorData;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller that is responsible for processing Vaadin endpoint requests.
 * Each class that is annotated with {@link Endpoint} gets its public methods
 * exposed so that those can be triggered by a correct POST request, including
 * the methods inherited from the other classes, excluding {@link Object} class
 * ones. Other methods (non-public) are not considered by the controller.
 * <p>
 * For example, if a class with name {@code TestClass} that has the only public
 * method {@code testMethod} was annotated with the annotation, it can be called
 * via {@literal http://${base_url}/testclass/testmethod} POST call, where
 * {@literal ${base_url}} is the application base url, configured by the user.
 * Class name and method name case in the request URL does not matter, but if
 * the method has parameters, the request body should contain a valid JSON with
 * all parameters in the same order as they are declared in the method. The
 * parameter types should also correspond for the request to be successful.
 */
@RestController
@Import({ VaadinConnectControllerConfiguration.class,
        VaadinEndpointProperties.class })
@ConditionalOnBean(annotation = Endpoint.class)
public class VaadinConnectController {
    static final String ENDPOINT_METHODS = "/{endpoint}/{method}";

    /**
     * A qualifier to override the request and response default json mapper.
     *
     * @see #VaadinConnectController(ObjectMapper, ExplicitNullableTypeChecker,
     *      ApplicationContext, EndpointRegistry)
     */
    public static final String VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER = "vaadinEndpointMapper";

    private final ObjectMapper vaadinEndpointMapper;
    private final Validator validator = Validation
            .buildDefaultValidatorFactory().getValidator();
    private final ExplicitNullableTypeChecker explicitNullableTypeChecker;
    private final ApplicationContext applicationContext;

    EndpointRegistry endpointRegistry;

    /**
     * A constructor used to initialize the controller.
     *
     * @param vaadinEndpointMapper
     *            optional bean to override the default {@link ObjectMapper}
     *            that is used for serializing and deserializing request and
     *            response bodies Use
     *            {@link VaadinConnectController#VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER}
     *            qualifier to override the mapper.
     * @param explicitNullableTypeChecker
     *            the method parameter and return value type checker to verify
     *            that null values are explicit
     * @param context
     *            Spring context to extract beans annotated with
     *            {@link Endpoint} from
     * @param endpointRegistry
     *            the registry used to store endpoint information
     */
    public VaadinConnectController(
            @Autowired(required = false) @Qualifier(VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER) ObjectMapper vaadinEndpointMapper,
            ExplicitNullableTypeChecker explicitNullableTypeChecker,
            ApplicationContext context, EndpointRegistry endpointRegistry) {
        this.applicationContext = context;
        this.vaadinEndpointMapper = vaadinEndpointMapper != null
                ? vaadinEndpointMapper
                : createVaadinConnectObjectMapper(context);
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        this.endpointRegistry = endpointRegistry;

        context.getBeansWithAnnotation(Endpoint.class)
                .forEach((name, endpointBean) -> endpointRegistry
                        .registerEndpoint(endpointBean));
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

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinConnectController.class);
    }

    /**
     * Captures and processes the Vaadin endpoint requests.
     * <p>
     * Matches the endpoint name and a method name with the corresponding Java
     * class and a public method in the class. Extracts parameters from a
     * request body if the Java method requires any and applies in the same
     * order. After the method call, serializes the Java method execution result
     * and sends it back.
     * <p>
     * If an issue occurs during the request processing, an error response is
     * returned instead of the serialized Java method return value.
     *
     * @param endpointName
     *            the name of an endpoint to address the calls to, not case
     *            sensitive
     * @param methodName
     *            the method name to execute on an endpoint, not case sensitive
     * @param body
     *            optional request body, that should be specified if the method
     *            called has parameters
     * @param request
     *            the current request which triggers the endpoint call
     * @return execution result as a JSON string or an error message string
     */
    @PostMapping(path = ENDPOINT_METHODS, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> serveEndpoint(
            @PathVariable("endpoint") String endpointName,
            @PathVariable("method") String methodName,
            @RequestBody(required = false) ObjectNode body,
            HttpServletRequest request) {
        getLogger().debug("Endpoint: {}, method: {}, request body: {}",
                endpointName, methodName, body);

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
                    VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER);
            getLogger().error(errorMessage, e);
            try {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseErrorObject(errorMessage));
            } catch (JsonProcessingException unexpected) {
                throw new IllegalStateException(String.format(
                        "Unexpected: Failed to serialize a plain Java string '%s' into a JSON. "
                                + "Double check the provided mapper's configuration.",
                        errorMessage), unexpected);
            }
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }
    }

    private ResponseEntity<String> invokeVaadinEndpointMethod(
            String endpointName, String methodName, Method methodToInvoke,
            ObjectNode body, VaadinEndpointData vaadinEndpointData,
            HttpServletRequest request) throws JsonProcessingException {
        VaadinConnectAccessChecker accessChecker = getAccessChecker(
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

    private String createResponseErrorObject(String errorMessage)
            throws JsonProcessingException {
        return vaadinEndpointMapper.writeValueAsString(Collections.singletonMap(
                EndpointException.ERROR_MESSAGE_FIELD, errorMessage));
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
            Type expectedType = javaParameters[i];
            try {
                Object parameter = vaadinEndpointMapper
                        .readerFor(vaadinEndpointMapper.getTypeFactory()
                                .constructType(expectedType))
                        .readValue(requestParameters.get(parameterNames[i]));

                endpointParameters[i] = parameter;

                if (parameter != null) {
                    constraintViolations.addAll(validator.validate(parameter));
                }
            } catch (IOException e) {
                String typeName = expectedType.getTypeName();
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
        private final VaadinConnectAccessChecker accessChecker;

        private VaadinConnectAccessCheckerWrapper(
                VaadinConnectAccessChecker checker) {
            accessChecker = checker;
        }
    }

    VaadinConnectAccessChecker getAccessChecker(ServletContext servletContext) {
        VaadinServletContext vaadinServletContext = new VaadinServletContext(
                servletContext);
        VaadinConnectAccessCheckerWrapper wrapper = vaadinServletContext
                .getAttribute(VaadinConnectAccessCheckerWrapper.class, () -> {
                    VaadinConnectAccessChecker accessChecker = applicationContext
                            .getBean(VaadinConnectAccessChecker.class);
                    ApplicationConfiguration cfg = ApplicationConfiguration
                            .get(vaadinServletContext);
                    if (cfg != null) {
                        accessChecker.enableCsrf(cfg.isXsrfProtectionEnabled());
                    }
                    return new VaadinConnectAccessCheckerWrapper(accessChecker);
                });
        return wrapper.accessChecker;
    }
}
