package com.vaadin.hilla.parser.plugins.backbone;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.util.IgnorePropertiesUtil;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.utils.Generics;

import org.jspecify.annotations.NonNull;

public final class PropertyPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private static SerializationConfig serializationConfig = new JacksonObjectMapperFactory.Json()
            .build()
            .setVisibility(PropertyAccessor.SETTER,
                    JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .setVisibility(PropertyAccessor.GETTER,
                    JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .getSerializationConfig();

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode) {
            var propertyNode = (PropertyNode) nodePath.getNode();
            propertyNode.setTarget(propertyNode.getSource().getName());
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof EntityNode) {

            var node = nodeDependencies.getNode();
            var model = (ClassInfoModel) node.getSource();
            if (model.isEnum()) {
                return nodeDependencies;
            }

            var properties = collectProperties(model)
                    .<Node<?, ?>> map(PropertyNode::of);

            return nodeDependencies.appendChildNodes(properties);
        }

        return nodeDependencies;
    }

    @Override
    public void setConfiguration(PluginConfiguration configuration) {
        super.setConfiguration(configuration);

        var factory = loadJacksonObjectMapperFactory();

        if (factory != null) {
            serializationConfig = factory.build().getSerializationConfig();
        }
    }

    public static Stream<JacksonPropertyModel> collectProperties(
            @NonNull ClassInfoModel model) {
        var cls = Objects.requireNonNull(model).get();

        if (!(cls instanceof Class<?>)) {
            throw new BackbonePluginException(
                    "Jackson: Only reflection models are supported");
        }

        var description = serializationConfig
                .introspect(serializationConfig.constructType((Class<?>) cls));

        var processor = new PropertyProcessor(description);
        return processor.stream();
    }

    private JacksonObjectMapperFactory loadJacksonObjectMapperFactory() {
        var config = getConfiguration();

        if (config != null
                && config.getObjectMapperFactoryClassName() != null) {
            Class<?> cls;

            try {
                cls = Class.forName(config.getObjectMapperFactoryClassName());
            } catch (ClassNotFoundException e) {
                throw new BackbonePluginException(
                        "ObjectMapper factory class is not found", e);
            }

            if (!JacksonObjectMapperFactory.class.isAssignableFrom(cls)) {
                throw new BackbonePluginException(String.format(
                        "Class %s does not implement JacksonObjectMapperFactory interface",
                        cls.getName()));
            }

            try {
                return (JacksonObjectMapperFactory) cls.getDeclaredConstructor()
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                throw new BackbonePluginException(
                        "Cannot instantiate ObjectMapper factory", e);
            }
        }

        return null;
    }

    /**
     * The class for processing each BeanDescription. The class' algorithm is
     * adopted from the Jackson's BeanSerializerFactory class (v2.15).
     */
    private static class PropertyProcessor {
        private final BeanDescription description;

        PropertyProcessor(BeanDescription description) {
            this.description = description;
        }

        public Stream<JacksonPropertyModel> stream() {
            var properties = description.findProperties().stream()
                    .map(JacksonPropertyModel::of);
            properties = filterPrivateProperties(properties);
            properties = filterSuperClassProperties(properties);
            properties = filterPropertiesWithIgnoredTypes(properties);

            // Remove ones without matching mutator (if it is configured).
            if (serializationConfig
                    .isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)) {
                properties = filterSetterlessGetters(properties);
            }

            return filterIgnoredProperties(properties);
        }

        /**
         * @see <a href=
         *      "https://github.com/FasterXML/jackson-databind/blob/2.15/src/main/java/com/fasterxml/jackson/databind/ser/BeanSerializerFactory.java#L652">BeanSerializerFactory#filterBeanProperties</a>
         */
        private Stream<JacksonPropertyModel> filterIgnoredProperties(
                Stream<JacksonPropertyModel> properties) {
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
        private Stream<JacksonPropertyModel> filterPropertiesWithIgnoredTypes(
                Stream<JacksonPropertyModel> properties) {
            var ignores = new HashMap<Class<?>, Boolean>();
            var introspector = serializationConfig.getAnnotationIntrospector();

            return properties.filter(property -> {
                var type = property.get().getRawPrimaryType();
                var result = ignores.get(type);

                if (result == null) {
                    result = serializationConfig.getConfigOverride(type)
                            .getIsIgnoredType();

                    if (result == null) {
                        var classInfo = serializationConfig
                                .introspectClassAnnotations(type)
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
         *      "https://github.com/FasterXML/jackson-databind/blob/2.15/src/main/java/com/fasterxml/jackson/databind/ser/BeanSerializerFactory.java#L802">BeanSerializerFactory#removeSetterlessGetters</a>
         */
        private Stream<JacksonPropertyModel> filterSetterlessGetters(
                Stream<JacksonPropertyModel> properties) {
            // one caveat: only remove implicit properties;
            // explicitly annotated ones should remain
            return properties.filter(property -> property.couldDeserialize()
                    || property.isExplicitlyIncluded());
        }

        private Stream<JacksonPropertyModel> filterPrivateProperties(
                Stream<JacksonPropertyModel> properties) {
            return properties.filter(
                    property -> !property.getAssociatedTypes().isEmpty());
        }

        private Stream<JacksonPropertyModel> filterSuperClassProperties(
                Stream<JacksonPropertyModel> properties) {
            return properties.filter(property -> property.getOwner()
                    .is(description.getBeanClass()));
        }

        private Set<String> findIgnored() {
            var ignored = serializationConfig.getDefaultPropertyIgnorals(
                    description.getBeanClass(), description.getClassInfo());
            return ignored == null ? null
                    : ignored.findIgnoredForSerialization();
        }

        private Set<String> findIncluded() {
            var included = serializationConfig.getDefaultPropertyInclusions(
                    description.getBeanClass(), description.getClassInfo());
            return included == null ? null : included.getIncluded();
        }
    }
}
