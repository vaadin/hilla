package dev.hilla;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.di.ResourceProvider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Checks if a given feature flag, defined using
 * {@link ConditionalOnFeatureFlag}, is enabled.
 */
public class FeatureFlagCondition implements Condition {

    private FeatureFlags featureFlags = null;

    @Override
    public boolean matches(ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        if (featureFlags == null) {
            featureFlags = getFeatureFlags(context);
        }
        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(
                        ConditionalOnFeatureFlag.class.getName());
        if (annotationAttributes == null) {
            throw new IllegalArgumentException(
                    getClass().getName() + " can only be used through an @"
                            + ConditionalOnFeatureFlag.class.getSimpleName()
                            + " annotation and none was found");
        }
        String featureId = (String) annotationAttributes.get("value");
        return featureFlags.isEnabled(featureId);
    }

    private FeatureFlags getFeatureFlags(ConditionContext context) {
        ClassLoader classLoader = Objects
                .requireNonNull(context.getClassLoader());

        ResourceProvider provider = new ResourceProvider() {

            @Override
            public URL getApplicationResource(String path) {
                return classLoader.getResource(path);
            }

            @Override
            public List<URL> getApplicationResources(String path)
                    throws IOException {
                return Collections.list(classLoader.getResources(path));
            }

            @Override
            public URL getClientResource(String path) {
                return getApplicationResource(path);
            }

            @Override
            public InputStream getClientResourceAsStream(String path)
                    throws IOException {
                return getClientResource(path).openStream();
            }
        };
        Lookup lookup = Lookup.of(provider, ResourceProvider.class);
        return new FeatureFlags(lookup);
    }
}
