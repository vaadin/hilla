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

package com.vaadin.fusion;

import java.lang.reflect.Method;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.fusion.auth.CsrfChecker;
import com.vaadin.fusion.auth.VaadinConnectAccessChecker;

/**
 * A configuration class for customizing the {@link VaadinConnectController}
 * class.
 */
@Configuration
public class VaadinConnectControllerConfiguration {
    private final VaadinEndpointProperties vaadinEndpointProperties;

    /**
     * Initializes the endpoint configuration.
     *
     * @param vaadinEndpointProperties
     *            Vaadin ednpoint properties
     */
    public VaadinConnectControllerConfiguration(
            VaadinEndpointProperties vaadinEndpointProperties) {
        this.vaadinEndpointProperties = vaadinEndpointProperties;
    }

    /**
     * Registers {@link VaadinConnectController} to use
     * {@link VaadinEndpointProperties#getVaadinEndpointPrefix()} as a prefix
     * for all Vaadin endpoints.
     *
     * @return updated configuration for {@link VaadinConnectController}
     */
    @Bean
    public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
        return new WebMvcRegistrations() {

            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {

                    @Override
                    protected void registerHandlerMethod(Object handler,
                            Method method, RequestMappingInfo mapping) {
                        // If Spring context initialization fails here with a
                        // stack overflow in a project that also has the
                        // `vaadin-spring` dependency, make sure that the Spring
                        // version in `flow-server` and in `vaadin-spring` is
                        // the same.

                        if (VaadinConnectController.class
                                .equals(method.getDeclaringClass())) {
                            mapping = prependEndpointPrefixUrl(mapping);
                        }

                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }
        };
    }

    /**
     * Prepends the endpoint prefix URL from the Vaadin properties to the
     * {@code pattern} of a {@link RequestMappingInfo} object, and returns the
     * updated mapping as a new object (not modifying the given {@param mapping}
     * parameter).
     *
     * @return a new mapping with the endpoint prefix URL prepended to the
     *         mapping pattern
     */
    private RequestMappingInfo prependEndpointPrefixUrl(
            RequestMappingInfo mapping) {
        PatternsRequestCondition connectEndpointPattern = new PatternsRequestCondition(
                vaadinEndpointProperties.getVaadinEndpointPrefix())
                        .combine(mapping.getPatternsCondition());

        return new RequestMappingInfo(mapping.getName(), connectEndpointPattern,
                mapping.getMethodsCondition(), mapping.getParamsCondition(),
                mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                mapping.getProducesCondition(), mapping.getCustomCondition());
    }

    /**
     * Registers an endpoint name checker responsible for validating the
     * endpoint names.
     *
     * @return the endpoint name checker
     */
    @Bean
    public EndpointNameChecker endpointNameChecker() {
        return new EndpointNameChecker();
    }

    /**
     * Registers a default {@link VaadinConnectAccessChecker} bean instance.
     *
     * @param accessAnnotationChecker
     *            the access controlks checker to use
     * @param csrfChecker
     *            the CSRF checker to use
     * @return the default Vaadin endpoint access checker bean
     */
    @Bean
    public VaadinConnectAccessChecker accessChecker(
            AccessAnnotationChecker accessAnnotationChecker,
            CsrfChecker csrfChecker) {
        return new VaadinConnectAccessChecker(accessAnnotationChecker,
                csrfChecker);
    }

    /**
     * Registers a default {@link AccessAnnotationChecker} bean instance.
     *
     * @return the default bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AccessAnnotationChecker accessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }

    /**
     * Registers a default {@link CsrfChecker} bean instance.
     *
     * @return the default bean
     */
    @Bean
    public CsrfChecker csrfChecker() {
        return new CsrfChecker();
    }

    /**
     * Registers a {@link ExplicitNullableTypeChecker} bean instance.
     *
     * @return the explicit nullable type checker
     */
    @Bean
    public ExplicitNullableTypeChecker typeChecker() {
        return new ExplicitNullableTypeChecker();
    }

    /**
     * Registers endpoint utility methods.
     *
     * @return the endpoint util class
     */
    @Bean
    public EndpointUtil endpointUtil() {
        return new EndpointUtil();
    }

    /**
     * Registers the endpoint registry.
     *
     * @param endpointNameChecker
     *            the name checker to use
     * @return the endpoint registry
     */
    @Bean
    public EndpointRegistry endpointRegistry(
            EndpointNameChecker endpointNameChecker) {
        return new EndpointRegistry(endpointNameChecker);
    }
}
