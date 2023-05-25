/**
 *    Copyright 2000-2023 Vaadin Ltd
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
package dev.hilla.gradle.plugin

import com.vaadin.gradle.getBooleanProperty
import org.gradle.api.Project
import java.io.File

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
     * Indicates whether the application should run in production mode, defaults to false.
     * Responds to the `-Philla.productionMode` property.
     */
    public var productionMode: Boolean = false

    /**
     * The folder where flow will put TS API files for client projects.
     */
    public var generatedTsFolder: File = File(project.projectDir, "frontend/generated")

    /**
     * Defines the output folder used by the project.
     *
     * Default value is the `project.buildDir` and should not need to be changed.
     */
    public var projectBuildDir: String = project.buildDir.toString()

    /**
     * The name of the SourceSet to scan for Vaadin components - i.e. the classes that are annotated with
     * Vaadin annotations.
     */
    public var sourceSetName : String = "main"

    /**
     * The node command to execute
     */
    public var nodeCommand : String = "node"


    public companion object {
        public fun get(project: Project): EngineProjectExtension =
          project.extensions.getByType(EngineProjectExtension::class.java)
    }

    internal fun autoconfigure(project: Project) {
        val productionModeProperty: Boolean? = project.getBooleanProperty("hilla.productionMode")
        if (productionModeProperty != null) {
            productionMode = productionModeProperty
        }
    }

    override fun toString(): String = "HillaPluginExtension(" +
            "exposedPackagesToParser=$exposedPackagesToParser, " +
            "productionMode=$productionMode, " +
            "generatedTsFolder=$generatedTsFolder, " +
            "sourceSetName=$sourceSetName, " +
            "nodeCommand=$nodeCommand, " +
            ")"
}
