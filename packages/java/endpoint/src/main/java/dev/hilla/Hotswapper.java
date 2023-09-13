package dev.hilla;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of updating internals of Hilla that need updates when application
 * classes are updated.
 */
public class Hotswapper {

    private static Logger getLogger() {
        return LoggerFactory.getLogger(Hotswapper.class);
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
                EndpointCodeGenerator.getInstance().update();
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
                || changedClass.startsWith("dev.hilla.parser.")
                || changedClass.startsWith("dev.hilla.engine.")
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
                || changedClass.startsWith("nonapi.io.github.classgraph.")
                || changedClass.startsWith("jdk.") || changedClass.equals(
                        "com.vaadin.base.devserver.viteproxy.ViteWebsocketProxy");
    }

    /**
     * Checks if changes in the given classes can affect the generated
     * TypeScript for endpoints.
     * 
     * @param changedClasses
     *            the changed classes
     * @return {@code true} if the classes can affect endpoint generation,
     *         {@code false} otherwise
     * @throws IOException
     */
    private static boolean affectsEndpoints(String[] changedClasses)
            throws IOException {
        Set<String> changedClassesSet = Set.of(changedClasses);
        Set<String> classesUsedInEndpoints = EndpointCodeGenerator.getInstance()
                .getClassesUsedInOpenApi();
        for (String classUsedInEndpoints : classesUsedInEndpoints) {
            if (changedClassesSet.contains(classUsedInEndpoints)) {
                getLogger().debug("The changed class " + classesUsedInEndpoints
                        + " is used in an endpoint");
                return true;
            }
        }

        for (String changedClass : changedClasses) {
            try {
                Class<?> cls = Class.forName(changedClass);
                if (cls.getAnnotation(Endpoint.class) != null
                        || cls.getAnnotation(EndpointExposed.class) != null) {
                    getLogger().debug(
                            "An endpoint annotation has been added to the class "
                                    + classesUsedInEndpoints);
                    return true;
                }

            } catch (ClassNotFoundException e) {
                getLogger().error("Unable to find class " + changedClass, e);
            }
        }
        return false;
    }
}
