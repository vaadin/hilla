package dev.hilla.parser.plugins.nonnull.nonnullapi;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER,
        ElementType.TYPE_USE })
public @interface Nullable {
}
