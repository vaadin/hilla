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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation to mark the endpoints to be processed by
 * {@link VaadinConnectController} class. Each class annotated automatically
 * becomes a Spring {@link Component} bean.
 *
 * After the class is annotated and processed, it becomes available as a Vaadin
 * endpoint. This means that the class name and all its public methods can be
 * executed via the post call with the correct parameters sent in a request JSON
 * body. The methods' return values will be returned back as a response to the
 * calls. Refer to {@link VaadinConnectController} for more details.
 *
 * @see VaadinConnectController
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
