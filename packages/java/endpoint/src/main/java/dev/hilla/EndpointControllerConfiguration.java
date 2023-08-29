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

package dev.hilla;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

import dev.hilla.auth.CsrfChecker;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.parser.jackson.JacksonObjectMapperFactory;

import jakarta.servlet.ServletContext;

/**
 * A configuration class for customizing the {@link EndpointController} class.
 */
@Configuration
public class EndpointControllerConfiguration {
    private final EndpointProperties endpointProperties;

    /**
     * Initializes the endpoint configuration.
     *
     * @param endpointProperties
     *            Vaadin ednpoint properties
     */
    public EndpointControllerConfiguration(
            EndpointProperties endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    /**
     * Registers a default {@link AccessAnnotationChecker} bean instance.
     *
     * @return the default bean
     */
    @Bean
    @ConditionalOnMissingBean
    AccessAnnotationChecker accessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }

    /**
     * Registers a default {@link EndpointAccessChecker} bean instance.
     *
     * @param accessAnnotationChecker
     *            the access controlks checker to use
     * @return the default Vaadin endpoint access checker bean
     */
    @Bean
    EndpointAccessChecker accessChecker(
            AccessAnnotationChecker accessAnnotationChecker) {
        return new EndpointAccessChecker(accessAnnotationChecker);
    }

    /**
     * Registers a default {@link CsrfChecker} bean instance.
     *
     * @param servletContext
     *            the servlet context
     *
     * @return the default bean
     */
    @Bean
    CsrfChecker csrfChecker(ServletContext servletContext) {
        return new CsrfChecker(servletContext);
    }

    /**
     * Registers the endpoint invoker.
     *
     * @param applicationContext
     *            Spring context to extract beans annotated with
     *            {@link Endpoint} from
     * @param endpointMapperFactory
     *            optional bean to override the default
     *            {@link JacksonObjectMapperFactory} that is used for
     *            serializing and deserializing request and response bodies Use
     *            {@link EndpointController#ENDPOINT_MAPPER_FACTORY_BEAN_QUALIFIER}
     *            qualifier to override the mapper.
     * @param explicitNullableTypeChecker
     *            the method parameter and return value type checker to verify
     *            that null values are explicit
     * @param servletContext
     *            the servlet context
     * @param endpointRegistry
     *            the registry used to store endpoint information
     *
     * @return the endpoint invoker
     */
    @Bean
    EndpointInvoker endpointInvoker(ApplicationContext applicationContext,
            @Autowired(required = false) @Qualifier(EndpointController.ENDPOINT_MAPPER_FACTORY_BEAN_QUALIFIER) JacksonObjectMapperFactory endpointMapperFactory,
            ExplicitNullableTypeChecker explicitNullableTypeChecker,
            ServletContext servletContext, EndpointRegistry endpointRegistry) {
        return new EndpointInvoker(applicationContext, endpointMapperFactory,
                explicitNullableTypeChecker, servletContext, endpointRegistry);
    }

    /**
     * Registers an endpoint name checker responsible for validating the
     * endpoint names.
     *
     * @return the endpoint name checker
     */
    @Bean
    EndpointNameChecker endpointNameChecker() {
        return new EndpointNameChecker();
    }

    /**
     * Registers the endpoint registry.
     *
     * @param endpointNameChecker
     *            the name checker to use
     * @return the endpoint registry
     */
    @Bean
    EndpointRegistry endpointRegistry(EndpointNameChecker endpointNameChecker) {
        return new EndpointRegistry(endpointNameChecker);
    }

    /**
     * Registers endpoint utility methods.
     *
     * @return the endpoint util class
     */
    @Bean
    EndpointUtil endpointUtil() {
        return new EndpointUtil();
    }

    /**
     * Registers a {@link ExplicitNullableTypeChecker} bean instance.
     *
     * @return the explicit nullable type checker
     */
    @Bean
    ExplicitNullableTypeChecker typeChecker() {
        return new ExplicitNullableTypeChecker();
    }

    /**
     * Registers {@link EndpointController} to use
     * {@link EndpointProperties#getEndpointPrefix()} as a prefix for all Vaadin
     * endpoints.
     *
     * @return updated configuration for {@link EndpointController}
     */
    @Bean
    WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
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

                        if (EndpointController.class
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
        RequestMappingInfo.Builder prefixMappingBuilder = RequestMappingInfo
                .paths(endpointProperties.getEndpointPrefix());
        if (mapping.getPatternsCondition() == null) {
            // `getPatternsCondition()` and `getPathPatternsCondition()` are
            // mutually exclusive: only one of them is active, the other
            // returns null. Since patterns condition is null here, let us
            // assume `mapping` uses parsed PathPatterns condition (default
            // since Spring Boot 2.6).
            //
            // However, `prefixMappingBuilder` uses non-parsed patterns
            // condition by default, which does not combine with parsed
            // PathPatterns condition in `RequestMappingInfo.combine()`.
            //
            // Set the pattern parser option for `prefixMappingBuilder`
            // to make it use parsed PathPatterns condition, so that
            // `.combine(mapping)` below works.
            RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
            options.setPatternParser(PathPatternParser.defaultInstance);
            prefixMappingBuilder.options(options);
        }
        return prefixMappingBuilder.build().combine(mapping);
    }

    /**
     * Can re-generate the TypeScipt code.
     * 
     * @param context
     *            the servlet context
     * @param endpointController
     *            the endpoint controller
     * @return the endpoint code generator
     */
    @Bean
    EndpointCodeGenerator endpointCodeGenerator(ServletContext servletContext,
            EndpointController endpointController) {
        return new EndpointCodeGenerator(
                new VaadinServletContext(servletContext), endpointController);
    }

}
