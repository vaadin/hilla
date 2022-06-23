package dev.hilla;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * A condition for registering components only when a given feature flag is
 * enabled.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(FeatureFlagCondition.class)
public @interface ConditionalOnFeatureFlag {

    /**
     * The id of the feature flag needed for the condition to be true.
     *
     * @return the feature id
     */
    String value();
}
