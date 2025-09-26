/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.hilla.EndpointInvocationException.EndpointBadRequestException;
import com.vaadin.hilla.EndpointInvocationException.EndpointForbiddenException;
import com.vaadin.hilla.EndpointInvocationException.EndpointHttpException;
import com.vaadin.hilla.EndpointInvocationException.EndpointInternalException;
import com.vaadin.hilla.EndpointInvocationException.EndpointNotFoundException;
import com.vaadin.hilla.EndpointInvocationException.EndpointUnauthorizedException;
import com.vaadin.hilla.EndpointRegistry.VaadinEndpointData;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import com.vaadin.hilla.exception.EndpointException;
import com.vaadin.hilla.exception.EndpointValidationException;
import com.vaadin.hilla.exception.EndpointValidationException.ValidationErrorData;
import jakarta.servlet.ServletContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNullApi;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles invocation of endpoint methods after checking the user has proper
 * access.
 * <p>
 * This class is a generic invoker that does not have knowledge of HTTP requests
 * or the context that the method is being invoked in.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class EndpointInvoker {
    private final ApplicationContext applicationContext;
    private final ObjectMapper endpointObjectMapper;
    private final EndpointRegistry endpointRegistry;
    private final ExplicitNullableTypeChecker explicitNullableTypeChecker;
    private final ServletContext servletContext;
    private final Validator validator;

    /**
     * Creates an instance of this bean.
     *
     * @param applicationContext
     *            The Spring application context
     * @param endpointObjectMapper
     *            The object mapper to be used for serialization and
     *            deserialization of request and response bodies. To override
     *            the mapper, use the
     *            {@link EndpointController#ENDPOINT_MAPPER_FACTORY_BEAN_QUALIFIER}
     *            qualifier on a JacksonObjectMapperFactory bean definition.
     * @param explicitNullableTypeChecker
     *            the method parameter and return value type checker to verify
     *            that null values are explicit
     * @param servletContext
     *            the servlet context
     * @param endpointRegistry
     *            the registry used to store endpoint information
     */
    public EndpointInvoker(ApplicationContext applicationContext,
            @Qualifier("hillaEndpointObjectMapper") ObjectMapper endpointObjectMapper,
            ExplicitNullableTypeChecker explicitNullableTypeChecker,
            ServletContext servletContext, EndpointRegistry endpointRegistry) {
        this.applicationContext = applicationContext;
        this.servletContext = servletContext;
        this.endpointObjectMapper = endpointObjectMapper;
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
        this.endpointRegistry = endpointRegistry;

        Validator validator = null;
        try {
            validator = applicationContext.getBean(Validator.class);
        } catch (Exception e) {
            getLogger().debug(
                    "Validator not found in Spring Context, will instantiate directly");
        }
        this.validator = validator == null
                ? Validation.buildDefaultValidatorFactory().getValidator()
                : validator;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(EndpointInvoker.class);
    }

    private boolean needsFloatToIntCoercion(JsonNode node, Type targetType) {
        if (node == null) {
            return false;
        }

        // Direct number node
        if (node.isNumber() && isIntegralType(targetType)
                && !node.canConvertToExactIntegral()) {
            return true;
        }

        // Array node containing floats to be converted to integral types
        if (node.isArray() && isIntegralType(targetType)) {
            for (JsonNode element : node) {
                if (element.isNumber()
                        && !element.canConvertToExactIntegral()) {
                    return true;
                }
            }
        }

        // Object node (for Maps and complex objects)
        if (node.isObject() && targetType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) targetType).getRawType();
            if (rawType instanceof Class<?>
                    && Map.class.isAssignableFrom((Class<?>) rawType)) {
                Type[] args = ((ParameterizedType) targetType)
                        .getActualTypeArguments();
                if (args.length > 1 && isIntegralType(args[1])) {
                    // Check if any map values need conversion
                    for (JsonNode value : node) {
                        if (value.isNumber()
                                && !value.canConvertToExactIntegral()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isIntegralType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> cls = (Class<?>) type;
            if (cls.isArray()) {
                // Handle array types
                return isIntegralType(cls.getComponentType());
            }
            return cls == int.class || cls == Integer.class || cls == long.class
                    || cls == Long.class || cls == short.class
                    || cls == Short.class || cls == byte.class
                    || cls == Byte.class || cls == java.math.BigInteger.class;
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> cls = (Class<?>) rawType;
                // Check for collections of integral types
                if (Collection.class.isAssignableFrom(cls)
                        || Map.class.isAssignableFrom(cls)) {
                    Type[] args = ((ParameterizedType) type)
                            .getActualTypeArguments();
                    if (args.length > 0) {
                        // For Map, check the value type (args[1])
                        int checkIndex = Map.class.isAssignableFrom(cls)
                                && args.length > 1 ? 1 : 0;
                        return isIntegralType(args[checkIndex]);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the return type of the given method.
     *
     * @param endpointName
     *            the name of the endpoint
     * @param methodName
     *            the name of the method
     */
    public Class<?> getReturnType(String endpointName, String methodName) {
        Method method = getMethod(endpointName, methodName);
        if (method == null) {
            getLogger().debug("Method '{}' not found in endpoint '{}'",
                    methodName, endpointName);
            return null;
        }
        return method.getReturnType();
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
     * @param principal
     *            the user principal object
     * @param rolesChecker
     *            a function for checking if a user is in a given role
     * @return the return value of the invoked endpoint method, wrapped in a
     *         response entity
     * @throws EndpointHttpException
     *             if thrown by the endpoint
     */
    public Object invoke(String endpointName, String methodName,
            ObjectNode body, Principal principal,
            Function<String, Boolean> rolesChecker)
            throws EndpointHttpException {
        VaadinEndpointData vaadinEndpointData = getVaadinEndpointData(
                endpointName);

        Method methodToInvoke = getMethod(endpointName, methodName);
        if (methodToInvoke == null) {
            getLogger().debug("Method '{}' not found in endpoint '{}'",
                    methodName, endpointName);
            throw new EndpointNotFoundException();
        }

        return invokeVaadinEndpointMethod(endpointName, methodName,
                methodToInvoke, body, vaadinEndpointData, principal,
                rolesChecker);

    }

    public VaadinEndpointData getVaadinEndpointData(String endpointName)
            throws EndpointNotFoundException {
        VaadinEndpointData vaadinEndpointData = endpointRegistry
                .get(endpointName);
        if (vaadinEndpointData == null) {
            getLogger().debug("Endpoint '{}' not found", endpointName);
            throw new EndpointNotFoundException();
        }
        return vaadinEndpointData;
    }

    String createResponseErrorObject(String errorMessage) {
        ObjectNode objectNode = endpointObjectMapper.createObjectNode();
        objectNode.put(EndpointException.ERROR_MESSAGE_FIELD, errorMessage);
        return objectNode.toString();
    }

    String createResponseErrorObject(Map<String, Object> serializationData)
            throws JacksonException {
        return endpointObjectMapper.writeValueAsString(serializationData);
    }

    EndpointAccessChecker getAccessChecker() {
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

    String writeValueAsString(Object returnValue) throws JacksonException {
        return endpointObjectMapper.writeValueAsString(returnValue);
    }

    private List<ValidationErrorData> createBeanValidationErrors(
            Collection<ConstraintViolation<Object>> beanConstraintViolations) {
        return beanConstraintViolations.stream().map(constraintViolation -> {
            String parameterPath = constraintViolation.getPropertyPath()
                    .toString();
            StringBuilder builder = new StringBuilder();
            builder.append("Object of type '")
                    .append(constraintViolation.getRootBeanClass());
            if (parameterPath != null && !parameterPath.isEmpty()) {
                builder.append("' has invalid property '")
                        .append(parameterPath);
            }
            builder.append("' with value '")
                    .append(constraintViolation.getInvalidValue())
                    .append("', validation error: '")
                    .append(constraintViolation.getMessage()).append("'");
            return new ValidationErrorData(builder.toString(), parameterPath,
                    constraintViolation.getMessage());
        }).collect(Collectors.toList());
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
                    constraintViolation.getMessage()), parameterPath,
                    constraintViolation.getMessage());
        }).collect(Collectors.toList());
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

    private Type[] getJavaParameters(Method methodToInvoke, Type classType) {
        return Stream.of(GenericTypeReflector
                .getExactParameterTypes(methodToInvoke, classType))
                .toArray(Type[]::new);
    }

    private Method getMethod(String endpointName, String methodName) {
        VaadinEndpointData endpointData = endpointRegistry.get(endpointName);
        if (endpointData == null) {
            getLogger().debug("Endpoint '{}' not found", endpointName);
            return null;
        }
        return endpointData.getMethod(methodName).orElse(null);
    }

    private Map<String, JsonNode> getRequestParameters(ObjectNode body,
            List<String> parameterNames) {
        // Respect the order of parameters in the request body
        Map<String, JsonNode> parametersData = new LinkedHashMap<>();
        if (body != null) {
            for (Map.Entry<String, JsonNode> entry : body.properties()) {
                parametersData.put(entry.getKey(), entry.getValue());
            }
        }

        // Try to adapt to the order of parameters in the method
        var orderedData = new LinkedHashMap<String, JsonNode>();
        for (String parameterName : parameterNames) {
            JsonNode parameterData = parametersData.get(parameterName);
            if (parameterData != null) {
                parametersData.remove(parameterName);
                orderedData.put(parameterName, parameterData);
            }
        }
        orderedData.putAll(parametersData);

        if (getLogger().isDebugEnabled()) {
            var returnedParameterNames = List.copyOf(orderedData.keySet());
            if (!parameterNames.equals(returnedParameterNames)) {
                getLogger().debug(
                        "The parameter names in the request body do not match the method parameters. Expected: {}, but got: {}",
                        parameterNames, returnedParameterNames);
            }
        }

        return orderedData;
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
                JsonNode parameterNode = requestParameters
                        .get(parameterNames[i]);
                Object parameter;

                // Jackson 3 limitation: TreeTraversingParser doesn't respect
                // ACCEPT_FLOAT_AS_INT
                // when deserializing from JsonNode. Convert to string for
                // numeric coercion.
                if (needsFloatToIntCoercion(parameterNode, parameterType)) {
                    // Convert JsonNode to string to allow float-to-int coercion
                    parameter = endpointObjectMapper
                            .readerFor(endpointObjectMapper.getTypeFactory()
                                    .constructType(incomingType))
                            .readValue(parameterNode.toString());
                } else {
                    parameter = endpointObjectMapper
                            .readerFor(endpointObjectMapper.getTypeFactory()
                                    .constructType(incomingType))
                            .readValue(parameterNode);
                }
                endpointParameters[i] = parameter;

                if (parameter != null) {
                    constraintViolations.addAll(validator.validate(parameter));
                }
            } catch (JacksonException e) {
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

    private ResponseEntity<String> handleMethodExecutionError(
            String endpointName, String methodName, InvocationTargetException e)
            throws EndpointHttpException {
        var wrappedException = e.getCause();
        if (wrappedException instanceof EndpointHttpException ex) {
            throw ex;
        } else if (EndpointException.class
                .isAssignableFrom(wrappedException.getClass())) {
            EndpointException endpointException = ((EndpointException) wrappedException);
            getLogger().debug("Endpoint '{}' method '{}' aborted the execution",
                    endpointName, methodName, endpointException);
            throw endpointException;
        } else {
            String errorMessage = String.format(
                    "Endpoint '%s' method '%s' execution failure", endpointName,
                    methodName);
            getLogger().error(errorMessage, e);
            throw new EndpointInternalException(errorMessage);
        }
    }

    public String checkAccess(EndpointRegistry.VaadinEndpointData endpointData,
            Method methodToInvoke, Principal principal,
            Function<String, Boolean> rolesChecker) {
        var methodDeclaringClass = methodToInvoke.getDeclaringClass();
        var invokedEndpointClass = ClassUtils
                .getUserClass(endpointData.getEndpointObject());
        EndpointAccessChecker accessChecker = getAccessChecker();
        String checkError;
        if (methodDeclaringClass.equals(invokedEndpointClass)) {
            checkError = accessChecker.check(methodToInvoke, principal,
                    rolesChecker);
        } else {
            checkError = accessChecker.check(invokedEndpointClass, principal,
                    rolesChecker);
        }
        return checkError;
    }

    private Object invokeVaadinEndpointMethod(String endpointName,
            String methodName, Method methodToInvoke, ObjectNode body,
            VaadinEndpointData vaadinEndpointData, Principal principal,
            Function<String, Boolean> rolesChecker)
            throws EndpointHttpException {
        HillaStats.reportEndpointActive();

        var checkError = checkAccess(vaadinEndpointData, methodToInvoke,
                principal, rolesChecker);
        if (checkError != null) {
            var message = String.format(
                    "Endpoint '%s' method '%s' request cannot be accessed, reason: '%s'",
                    endpointName, methodName, checkError);
            if (principal == null) {
                throw new EndpointUnauthorizedException(message);
            } else {
                throw new EndpointForbiddenException(message);
            }
        }

        var parameterNames = Arrays.stream(methodToInvoke.getParameters())
                .map(Parameter::getName).toList();
        Map<String, JsonNode> requestParameters = getRequestParameters(body,
                parameterNames);
        Type[] javaParameters = getJavaParameters(methodToInvoke, ClassUtils
                .getUserClass(vaadinEndpointData.getEndpointObject()));
        if (javaParameters.length != requestParameters.size()) {
            throw new EndpointBadRequestException(String.format(
                    "Incorrect number of parameters for endpoint '%s' method '%s', "
                            + "expected: %s, got: %s",
                    endpointName, methodName, javaParameters.length,
                    requestParameters.size()));
        }

        Object[] vaadinEndpointParameters = getVaadinEndpointParameters(
                requestParameters, javaParameters, methodName, endpointName);

        Set<ConstraintViolation<Object>> methodParameterConstraintViolations = validator
                .forExecutables()
                .validateParameters(vaadinEndpointData.getEndpointObject(),
                        methodToInvoke, vaadinEndpointParameters);
        if (!methodParameterConstraintViolations.isEmpty()) {
            throw new EndpointValidationException(
                    String.format(
                            "Validation error in endpoint '%s' method '%s'",
                            endpointName, methodName),
                    createMethodValidationErrors(
                            methodParameterConstraintViolations));
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
            throw new EndpointBadRequestException(errorMessage);
        } catch (IllegalAccessException e) {
            String errorMessage = String.format(
                    "Endpoint '%s' method '%s' access failure", endpointName,
                    methodName);
            getLogger().error(errorMessage, e);
            throw new EndpointInternalException(errorMessage);
        } catch (InvocationTargetException e) {
            return handleMethodExecutionError(endpointName, methodName, e);
        }

        String implicitNullError = this.explicitNullableTypeChecker
                .checkValueForAnnotatedElement(returnValue, methodToInvoke,
                        isNonNullApi(methodToInvoke.getDeclaringClass()
                                .getPackage()));
        if (implicitNullError != null) {
            String errorMessage = String.format(
                    "Unexpected return value in endpoint '%s' method '%s'. %s",
                    endpointName, methodName, implicitNullError);
            getLogger().error(errorMessage);
            throw new EndpointInternalException(errorMessage);
        }

        Set<ConstraintViolation<Object>> returnValueConstraintViolations = validator
                .forExecutables()
                .validateReturnValue(vaadinEndpointData.getEndpointObject(),
                        methodToInvoke, returnValue);
        if (!returnValueConstraintViolations.isEmpty()) {
            String errorMessage = String.format(
                    "Endpoint '%s' method '%s' returned a value that has validation errors: '%s'",
                    endpointName, methodName, returnValueConstraintViolations);
            throw new EndpointInternalException(errorMessage);
        }

        return returnValue;
    }

    private boolean isNonNullApi(Package pkg) {
        return Stream.of(pkg.getAnnotations())
                .anyMatch(ann -> ann.annotationType().getSimpleName()
                        .equals(NonNullApi.class.getSimpleName()));
    }

    private String listMethodParameterTypes(Type[] javaParameters) {
        return Stream.of(javaParameters).map(Type::getTypeName)
                .collect(Collectors.joining(", "));
    }

    private static class VaadinConnectAccessCheckerWrapper {
        private final EndpointAccessChecker accessChecker;

        private VaadinConnectAccessCheckerWrapper(
                EndpointAccessChecker checker) {
            accessChecker = checker;
        }
    }

}
