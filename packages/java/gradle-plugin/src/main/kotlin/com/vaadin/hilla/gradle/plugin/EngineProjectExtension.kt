/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.hilla.gradle.plugin

import org.gradle.api.Project

public open class EngineProjectExtension(project: Project) {

    /**
     * The packages to scan for classes annotated with @Endpoint and
     * related model classes. For a single module application, there is
     * no need for configuring this necessarily, as the classes from the
     * current project is automatically scanned (unless the endpoints are
     * located in a dependency).
     * On the contrary, in a multi-module project if the endpoint classes
     * are located in the other modules, it is necessary to set the list
     * packages in the build.gradle file.
     */
    public var exposedPackagesToParser: List<String> = mutableListOf()

    /**
     * The node command to execute
     */
    public var nodeCommand : String = "node"


    public companion object {
        public fun get(project: Project): EngineProjectExtension =
          project.extensions.getByType(EngineProjectExtension::class.java)
    }

    override fun toString(): String = "HillaPluginExtension(" +
            "exposedPackagesToParser=$exposedPackagesToParser, " +
            "nodeCommand=$nodeCommand" +
            ")"
}
