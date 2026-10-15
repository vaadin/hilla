/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation to mark the endpoints to be processed by
 * {@link EndpointController} class. Each class annotated automatically becomes
 * a Spring {@link Component} bean.
 *
 * After the class is annotated and processed, it becomes available as a Vaadin
 * endpoint. This means that the class name and all its public methods can be
 * executed via the post call with the correct parameters sent in a request JSON
 * body. The methods' return values will be returned back as a response to the
 * calls. Refer to {@link EndpointController} for more details.
 * <p>
 * This is an alias for {@link BrowserCallable}.
 *
 * @see EndpointController
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Endpoint {
    /**
     * The name of an endpoint to use. If nothing is specified, the name of the
     * annotated class is taken.
     * <p>
     * Note: custom names are not allowed to be blank, be equal to any of the
     * ECMAScript reserved words or have whitespaces in them. See
     * {@link EndpointNameChecker} for validation implementation details.
     *
     * @return the name of the endpoint to use in post requests
     */
    String value() default "";
}
