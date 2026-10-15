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
 * Makes the methods of the annotated class available to the browser.
 * <p>
 * For each class, a corresponding TypeScript class is generated in
 * {@code src/main/frontend/generated} with TypeScript methods for invoking the
 * methods in this class.
 * <p>
 * This is an alias for {@link Endpoint}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface BrowserCallable {
}
