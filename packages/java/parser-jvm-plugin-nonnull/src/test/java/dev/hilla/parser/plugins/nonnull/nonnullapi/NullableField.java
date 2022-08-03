package dev.hilla.parser.plugins.nonnull.nonnullapi;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NullableField {
}
