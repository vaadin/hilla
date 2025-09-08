package com.vaadin.hilla.transfertypes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a model module reference for transfer types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModelFromModule {
    /**
     * The module name.
     */
    String module();

    /**
     * The named specifier from the module.
     */
    String namedSpecifier() default "";

    /**
     * The default specifier from the module.
     */
    String defaultSpecifier() default "";
}
