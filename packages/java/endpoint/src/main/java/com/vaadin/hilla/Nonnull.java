/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark either field, method, parameter or type parameter as
 * non-nullable. It is used by Typescript Generator as a source of type
 * nullability information.
 *
 * This annotation exists only for convenience because the traditional
 * `jakarta.annotation.Nonnull` annotation is not applicable to type parameters.
 *
 * @deprecated use the standardized {@link org.jspecify.annotations.NonNull}
 *             instead
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE })
@Deprecated(forRemoval = true)
public @interface Nonnull {
}
