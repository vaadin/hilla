package com.vaadin.hilla;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.hotswap.VaadinHotswapper;
import com.vaadin.flow.server.VaadinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of updating internals of Hilla that need updates when application
 * classes are updated.
 */
public class Hotswapper implements VaadinHotswapper {

    private static boolean inUse;

    private static Logger getLogger() {
        return LoggerFactory.getLogger(Hotswapper.class);
    }

    @Override
    public boolean onClassLoadEvent(VaadinService vaadinService,
            Set<Class<?>> classes, boolean redefined) {
        onHotswap(redefined,
                classes.stream().map(Class::getName).toArray(String[]::new));
        return false;
    }

    /**
     * Called by hot swap solutions when one or more classes have been updated.
     * <p>
     * The hot swap solution should not pre-filter the classes but pass
     * everything to this method.
     *
     * @param redefined
     *            {@code true} if the class was redefined, {@code false} if it
     *            was loaded for the first time
     * @param changedClasses
     *            the classes that have been added or modified
     */
    public static void onHotswap(Boolean redefined, String[] changedClasses) {
        try {
            if (isIgnoredClasses(changedClasses)) {
                return;
            }
            if (affectsEndpoints(changedClasses)) {
                if (getLogger().isDebugEnabled()) {
                    String changed = List.of(changedClasses).toString();
                    String operation = redefined ? "updated" : "added";
                    getLogger().debug("Regenerating endpoints because "
                            + changed + " were " + operation);
                }
                EndpointCodeGenerator.getInstance().update(changedClasses);
            }
        } catch (IOException e) {
            getLogger().error("Failed to re-generated TypeScript code");
        }
    }

    private static boolean isIgnoredClasses(String[] changedClasses) {
        for (String changedClass : changedClasses) {
            if (!isIgnoredClass(changedClass)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIgnoredClass(String changedClass) {
        return changedClass.startsWith("java.")
                || changedClass.startsWith("javax.")
                || changedClass.startsWith("sun.")
                || changedClass.startsWith("com.sun.")
                || changedClass.startsWith("com.vaadin.hilla.typescript.parser.")
                || changedClass.startsWith("com.vaadin.hilla.engine.")
                || changedClass.startsWith("com.vaadin.flow.")
                || changedClass.startsWith("com.vaadin.base.")
                || changedClass.startsWith("org.jsoup.")
                || changedClass.startsWith("io.github.classgraph.")
                || changedClass.startsWith("io.swagger.")
                || changedClass.startsWith("com.fasterxml.jackson.")
                || changedClass.startsWith("elemental.json.")
                || changedClass.startsWith("org.springframework.")
                || changedClass.startsWith("org.atmosphere.")
                || changedClass.startsWith("org.apache.commons.")
                || changedClass.startsWith("org.apache.coyote.")
                || changedClass.startsWith("org.apache.catalina.")
                || changedClass.startsWith("org.apache.tomcat.")
                || changedClass.startsWith("org.hotswap.")
                || changedClass.startsWith("jakarta.")
                || changedClass.startsWith("io.gprc.")
                || changedClass.startsWith("io.netty.")
                || changedClass.startsWith("com.google.gson.")
                || changedClass.startsWith("nonapi.io.github.classgraph.")
                || changedClass.startsWith("jdk.") || changedClass.equals(
                        "com.vaadin.base.devserver.viteproxy.ViteWebsocketProxy");
    }

    /**
     * Checks if changes in the given classes can affect the generated
     * TypeScript for endpoints.
     *
     * Note: Since OpenAPI generation has been removed, this method now simply
     * checks if any changed class has endpoint annotations. For a more
     * comprehensive check, consider tracking used types during TypeScript
     * generation.
     *
     * @param changedClasses
     *            the changed classes
     * @return {@code true} if the classes can affect endpoint generation,
     *         {@code false} otherwise
     * @throws IOException
     */
    private static boolean affectsEndpoints(String[] changedClasses)
            throws IOException {
        // Check if any changed class has endpoint annotations
        for (String changedClass : changedClasses) {
            try {
                Class<?> cls = Class.forName(changedClass);
                if (cls.getAnnotation(Endpoint.class) != null
                        || cls.getAnnotation(BrowserCallable.class) != null
                        || cls.getAnnotation(EndpointExposed.class) != null) {
                    getLogger().debug(
                            "An endpoint annotation has been added to the class "
                                    + changedClass);
                    return true;
                }

            } catch (ClassNotFoundException e) {
                getLogger().error("Unable to find class " + changedClass, e);
            }
        }

        // Conservative approach: regenerate for any non-ignored class change
        // This ensures we don't miss parameter/return types used by endpoints
        getLogger().debug(
                "Changed classes might be used by endpoints, triggering regeneration");
        return true;
    }

    public static void markInUse() {
        Hotswapper.inUse = true;
    }

    public static boolean isInUse() {
        return inUse;
    }
}
