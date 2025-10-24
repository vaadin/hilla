package com.vaadin.hilla.typescript.codegen;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object for sharing state between TypeScript generator plugins during
 * code generation.
 */
public class GenerationContext {
    private final Map<String, Object> attributes = new HashMap<>();
    private final String outputDirectory;

    /**
     * Creates a new generation context.
     *
     * @param outputDirectory
     *            the output directory for generated files
     */
    public GenerationContext(@NonNull String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Gets the output directory for generated files.
     *
     * @return the output directory path
     */
    @NonNull
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets an attribute in the context.
     *
     * @param key
     *            the attribute key
     * @param value
     *            the attribute value
     */
    public void setAttribute(@NonNull String key, @Nullable Object value) {
        attributes.put(key, value);
    }

    /**
     * Gets an attribute from the context.
     *
     * @param key
     *            the attribute key
     * @return an optional containing the attribute value, or empty if not found
     */
    @NonNull
    public Optional<Object> getAttribute(@NonNull String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    /**
     * Gets an attribute from the context with a specific type.
     *
     * @param key
     *            the attribute key
     * @param type
     *            the expected type
     * @param <T>
     *            the type parameter
     * @return an optional containing the typed attribute value, or empty if not
     *         found or wrong type
     */
    @NonNull
    public <T> Optional<T> getAttribute(@NonNull String key,
            @NonNull Class<T> type) {
        return getAttribute(key).filter(type::isInstance).map(type::cast);
    }

    /**
     * Checks if an attribute exists in the context.
     *
     * @param key
     *            the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(@NonNull String key) {
        return attributes.containsKey(key);
    }

    /**
     * Removes an attribute from the context.
     *
     * @param key
     *            the attribute key
     * @return the removed value, or empty if not found
     */
    @NonNull
    public Optional<Object> removeAttribute(@NonNull String key) {
        return Optional.ofNullable(attributes.remove(key));
    }

    /**
     * Clears all attributes from the context.
     */
    public void clear() {
        attributes.clear();
    }
}
