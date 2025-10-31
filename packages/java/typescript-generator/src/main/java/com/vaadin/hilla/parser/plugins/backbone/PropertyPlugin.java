package com.vaadin.hilla.parser.plugins.backbone;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.util.IgnorePropertiesUtil;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;

import org.jspecify.annotations.NonNull;

public final class PropertyPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    // Create a mapper with visibility configuration and get its serialization
    // config
    // Disable alphabetical sorting to preserve declaration order
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(vc -> vc
                    .withVisibility(PropertyAccessor.SETTER,
                            JsonAutoDetect.Visibility.PUBLIC_ONLY)
                    .withVisibility(PropertyAccessor.GETTER,
                            JsonAutoDetect.Visibility.PUBLIC_ONLY))
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build();

    private SerializationConfig serializationConfig = MAPPER
            .serializationConfig();

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
        if (!(nodeDependencies.getNode() instanceof EntityNode)) {
            return nodeDependencies;
        }

        var node = nodeDependencies.getNode();
        var model = (ClassInfoModel) node.getSource();
        if (model.isEnum()) {
            return nodeDependencies;
        }

        var properties = collectProperties(model)
                .<Node<?, ?>> map(PropertyNode::of);

        return nodeDependencies.appendChildNodes(properties);
    }

    @Override
    public void setConfiguration(PluginConfiguration configuration) {
        super.setConfiguration(configuration);

        var factory = loadJacksonObjectMapperFactory();

        if (factory != null) {
            var mapper = factory.build();
            // Disable alphabetical sorting to preserve declaration order
            ObjectMapper jsonMapper = mapper.rebuild()
                    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .build();
            this.serializationConfig = jsonMapper.serializationConfig();
        }
    }

    private Stream<JacksonPropertyModel> collectProperties(
            @NonNull ClassInfoModel model) {
        var cls = Objects.requireNonNull(model).get();

        if (!(cls instanceof Class<?>)) {
            throw new BackbonePluginException(
                    "Jackson: Only reflection models are supported");
        }

        // In Jackson 3, use classIntrospectorInstance to get introspector
        var javaType = serializationConfig.constructType((Class<?>) cls);
        var introspector = serializationConfig.classIntrospectorInstance();
        var annotatedClass = introspector.introspectClassAnnotations(javaType);
        var description = introspector.introspectForSerialization(javaType,
                annotatedClass);

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
    private class PropertyProcessor {
        private final BeanDescription description;

        PropertyProcessor(BeanDescription description) {
            this.description = description;
        }

        public Stream<JacksonPropertyModel> stream() {
            var properties = description.findProperties().stream()
                    .map((BeanPropertyDefinition prop) -> JacksonPropertyModel
                            .of(prop));
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
                var type = property.get().getPrimaryType().getRawClass();
                var result = ignores.get(type);

                if (result == null) {
                    result = serializationConfig.getConfigOverride(type)
                            .getIsIgnoredType();

                    if (result == null) {
                        // In Jackson 3, use classIntrospectorInstance to get
                        // introspector
                        var javaType = serializationConfig.constructType(type);
                        var classIntrospector = serializationConfig
                                .classIntrospectorInstance();
                        var annotatedClass = classIntrospector
                                .introspectClassAnnotations(javaType);
                        var classDesc = classIntrospector
                                .introspectForSerialization(javaType,
                                        annotatedClass);
                        var classInfo = classDesc.getClassInfo();
                        result = introspector.isIgnorableType(
                                serializationConfig, classInfo);

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
