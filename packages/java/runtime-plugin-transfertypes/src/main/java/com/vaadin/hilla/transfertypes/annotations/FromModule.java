package com.vaadin.hilla.transfertypes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FromModule {
    String module();
    String[] namedSpecifiers();
    String defaultSpecifier();
}
