/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package dev.hilla;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.hilla.EndpointInvocationException.EndpointAccessDeniedException;
import dev.hilla.EndpointInvocationException.EndpointBadRequestException;
import dev.hilla.EndpointInvocationException.EndpointInternalException;
import dev.hilla.EndpointInvocationException.EndpointNotFoundException;
import dev.hilla.auth.CsrfChecker;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.exception.EndpointException;

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
@Import({ EndpointControllerConfiguration.class, EndpointProperties.class })
@NpmPackage(value = "@hilla/frontend", version = "1.1.14")
@NpmPackage(value = "@hilla/form", version = "1.1.14")
public class EndpointController {
    static final String ENDPOINT_METHODS = "/{endpoint}/{method}";

    /**
     * A qualifier to override the request and response default json mapper.
     */
    public static final String VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER = "vaadinEndpointMapper";

    EndpointRegistry endpointRegistry;

    private CsrfChecker csrfChecker;

    private EndpointInvoker endpointInvoker;

    /**
     * A constructor used to initialize the controller.
     *
     * @param context
     *            Spring context to extract beans annotated with
     *            {@link Endpoint} from
     * @param endpointRegistry
     *            the registry used to store endpoint information
     * @param endpointInvoker
     *            then end point invoker
     * @param csrfChecker
     *            the csrf checker to use
     */
    public EndpointController(ApplicationContext context,
            EndpointRegistry endpointRegistry, EndpointInvoker endpointInvoker,
            CsrfChecker csrfChecker) {
        this.endpointInvoker = endpointInvoker;
        this.csrfChecker = csrfChecker;
        this.endpointRegistry = endpointRegistry;

        context.getBeansWithAnnotation(Endpoint.class)
                .forEach((name, endpointBean) -> endpointRegistry
                        .registerEndpoint(endpointBean));
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(EndpointController.class);
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

        if (!csrfChecker.validateCsrfTokenInRequest(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(endpointInvoker.createResponseErrorObject(
                            EndpointAccessChecker.ACCESS_DENIED_MSG));
        }

        try {
            // Put a VaadinRequest in the instances object so as the request is
            // available in the end-point method
            VaadinServletService service = (VaadinServletService) VaadinService
                    .getCurrent();
            CurrentInstance.set(VaadinRequest.class,
                    new VaadinServletRequest(request, service));
            Object returnValue = endpointInvoker.invoke(endpointName,
                    methodName, body, request.getUserPrincipal(),
                    request::isUserInRole);
            try {
                return ResponseEntity
                        .ok(endpointInvoker.writeValueAsString(returnValue));
            } catch (JsonProcessingException e) {
                String errorMessage = String.format(
                        "Failed to serialize endpoint '%s' method '%s' response. "
                                + "Double check method's return type or specify a custom mapper bean with qualifier '%s'",
                        endpointName, methodName,
                        EndpointController.VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER);
                getLogger().error(errorMessage, e);
                throw new EndpointInternalException(errorMessage);
            }
        } catch (EndpointException e) {
            try {
                return ResponseEntity.badRequest().body(endpointInvoker
                        .createResponseErrorObject(e.getSerializationData()));
            } catch (JsonProcessingException ee) {
                String errorMessage = String.format(
                        "Failed to serialize error object for endpoint exception. ");
                getLogger().error(errorMessage, e);
                return ResponseEntity.internalServerError().body(errorMessage);
            }
        } catch (EndpointNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EndpointAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    endpointInvoker.createResponseErrorObject(e.getMessage()));
        } catch (EndpointBadRequestException e) {
            return ResponseEntity.badRequest().body(
                    endpointInvoker.createResponseErrorObject(e.getMessage()));
        } catch (EndpointInternalException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    endpointInvoker.createResponseErrorObject(e.getMessage()));
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }

    }

}
