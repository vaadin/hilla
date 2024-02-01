/**
 *    Copyright 2000-2024 Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
