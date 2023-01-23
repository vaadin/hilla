package dev.hilla.parser.plugins.backbone;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.util.IgnorePropertiesUtil;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.jackson.JacksonPropertyModel;

import jakarta.annotation.Nonnull;

/**
 * The class collects all properties from the class according to Jackson's
 * annotation logic.
 */
final class JacksonPropertyCollector {
    private static final Comparator<BeanPropertyDefinition> sorter = Comparator
            .comparing(BeanPropertyDefinition::getName);
    private final SerializationConfig config;

    public JacksonPropertyCollector(@Nonnull ObjectMapper mapper) {
        this.config = Objects.requireNonNull(mapper).getSerializationConfig();
    }

    public Stream<JacksonPropertyModel> collectFrom(
            @Nonnull ClassInfoModel model) {
        var cls = Objects.requireNonNull(model).get();

        if (!(cls instanceof Class<?>)) {
            throw new BackbonePluginException(
                    "Jackson: Only reflection models are supported");
        }

        var description = config
                .introspect(config.constructType((Class<?>) cls));

        var processor = new PropertyProcessor(description);
        return processor.stream().sorted(sorter).map(JacksonPropertyModel::of);
    }

    /**
     * The class for processing each BeanDescription. The class' algorithm is
     * adopted from the Jackson's BeanSerializerFactory class (v2.15).
     */
    private class PropertyProcessor {
        private final BeanDescription description;

        PropertyProcessor(BeanDescription description) {
            this.description = description;
        }

        public Stream<BeanPropertyDefinition> stream() {
            var properties = description.findProperties().stream();
            properties = filterSuperClassProperties(properties);
            properties = filterPropertiesWithIgnoredTypes(properties);

            // and possibly remove ones without matching mutator...
            if (config.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)) {
                properties = filterSetterlessGetters(properties);
            }

            return filterIgnoredProperties(properties);
        }

        /**
         * @see <a href=
         *      "https://github.com/FasterXML/jackson-databind/blob/2.15/src/main/java/com/fasterxml/jackson/databind/ser/BeanSerializerFactory.java#L652">BeanSerializerFactory#filterBeanProperties</a>
         */
        private Stream<BeanPropertyDefinition> filterIgnoredProperties(
                Stream<BeanPropertyDefinition> properties) {
            var ignored = findIgnored();
            var included = findIncluded();

            if (included != null || (ignored != null && !ignored.isEmpty())) {
                properties = properties.filter(property -> !IgnorePropertiesUtil
                        .shouldIgnore(property.getName(), ignored, included));
            }

            return properties;
        }

        /**
         * @see <a href=
         *      "https://github.com/FasterXML/jackson-databind/blob/2.15/src/main/java/com/fasterxml/jackson/databind/ser/BeanSerializerFactory.java#L759">BeanSerializerFactory#removeIgnorableTypes</a>
         */
        private Stream<BeanPropertyDefinition> filterPropertiesWithIgnoredTypes(
                Stream<BeanPropertyDefinition> properties) {
            var ignores = new HashMap<Class<?>, Boolean>();
            var introspector = config.getAnnotationIntrospector();

            return properties.filter(property -> {
                var accessor = property.getAccessor();

                if (accessor == null) {
                    return false;
                }

                var type = property.getRawPrimaryType();
                var result = ignores.get(type);

                if (result == null) {
                    result = config.getConfigOverride(type).getIsIgnoredType();

                    if (result == null) {
                        var classInfo = config.introspectClassAnnotations(type)
                                .getClassInfo();
                        result = introspector.isIgnorableType(classInfo);

                        if (result == null) {
                            result = Boolean.FALSE;
                        }
                    }
                    ignores.put(type, result);
                }

                return !result;
            });
        }

        /**
         * @see <a href=
         *      "https://github.com/FasterXML/jackson-databind/blob/2.15/src/main/java/com/fasterxml/jackson/databind/ser/BeanSerializerFactory.java#L696">BeanSerializerFactory#removeSetterlessGetters</a>
         */
        private Stream<BeanPropertyDefinition> filterSetterlessGetters(
                Stream<BeanPropertyDefinition> properties) {
            // one caveat: only remove implicit properties;
            // explicitly annotated ones should remain
            return properties.filter(property -> property.couldDeserialize()
                    || property.isExplicitlyIncluded());
        }

        private Stream<BeanPropertyDefinition> filterSuperClassProperties(
                Stream<BeanPropertyDefinition> properties) {
            return properties.filter(
                    property -> Objects.equals(description.getBeanClass(),
                            property.getAccessor().getDeclaringClass()));
        }

        private Set<String> findIgnored() {
            var ignored = config.getDefaultPropertyIgnorals(
                    description.getBeanClass(), description.getClassInfo());
            return ignored == null ? null
                    : ignored.findIgnoredForSerialization();
        }

        private Set<String> findIncluded() {
            var included = config.getDefaultPropertyInclusions(
                    description.getBeanClass(), description.getClassInfo());
            return included == null ? null : included.getIncluded();
        }
    }
}
