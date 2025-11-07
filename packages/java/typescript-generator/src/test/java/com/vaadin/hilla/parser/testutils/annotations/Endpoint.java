package com.vaadin.hilla.parser.testutils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to mark endpoint classes. Supports optional value for custom
 * endpoint names.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Endpoint {
    String value() default "";
}
