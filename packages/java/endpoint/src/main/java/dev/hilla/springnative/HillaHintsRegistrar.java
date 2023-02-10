package dev.hilla.springnative;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.hilla.push.PushEndpoint;
import dev.hilla.push.messages.fromclient.AbstractServerMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Registers runtime hints for Spring 3 native support for Hilla.
 */
public class HillaHintsRegistrar implements RuntimeHintsRegistrar {

    private static final String GENERATED_OPENAPI_JSON = "target/generated-resources/openapi.json";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerEndpointTypes(hints);

        hints.reflection().registerType(PushEndpoint.class,
                MemberCategory.values());

        List<Class<?>> pushMessageTypes = new ArrayList<>();
        pushMessageTypes.addAll(getMessageTypes(AbstractServerMessage.class));
        pushMessageTypes.addAll(getMessageTypes(AbstractClientMessage.class));
        for (Class<?> cls : pushMessageTypes) {
            hints.reflection().registerType(cls, MemberCategory.values());
        }
    }

    private void registerEndpointTypes(RuntimeHints hints) {
        File openApiFile = new File(GENERATED_OPENAPI_JSON);
        if (!openApiFile.exists()) {
            logger.warn(
                    "Open API definition not found in {}. Endpoint types will not be registered",
                    openApiFile.getAbsoluteFile());
            return;
        }

        try {
            String openApiAsText = FileUtils.readFileToString(openApiFile,
                    StandardCharsets.UTF_8);
            List<Class<?>> types = parseOpenApi(openApiAsText);
            for (Class<?> type : types) {
                hints.reflection().registerType(type,
                        MemberCategory.values());
            }
        } catch (IOException e) {
            logger.error("Error while scanning and registering endpoint types",
                    e);
        }
    }

    /**
     * Parses the given open api and finds the used custom types.
     * 
     * @param openApiAsText the open api JSON as text
     * @return a list of custom types used
     * @throws IOException if parsing fails
     */
    public static List<Class<?>> parseOpenApi(String openApiAsText) throws IOException {
        JsonNode openApi = new ObjectMapper().readTree(openApiAsText);
        ObjectNode schemas = (ObjectNode) openApi.get("components")
                .get("schemas");
        List<Class<?>> types = new ArrayList<>();
        schemas.fieldNames().forEachRemaining(type -> {
            try {
                types.add(Class.forName(type));
            } catch (ClassNotFoundException e) {
                // The type in openapi.json is currently the canonical name so if it is an inner
                // class it should use $ instead of .
                int lastDot = type.lastIndexOf('.');
                if (lastDot >= 0) {
                    String modifiedName = type.substring(0, lastDot) + "$" + type.substring(lastDot + 1);
                    try {
                        types.add(Class.forName(modifiedName));
                    } catch (ClassNotFoundException ee) {
                    }
                }
            }
        });
        return types;

    }

    private Collection<Class<?>> getMessageTypes(Class<?> cls) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(cls);
        JsonSubTypes subTypes = cls.getAnnotation(JsonSubTypes.class);
        for (JsonSubTypes.Type t : subTypes.value()) {
            classes.add(t.value());
        }
        return classes;
    }

}
