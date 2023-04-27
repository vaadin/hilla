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

import org.gradle.api.Project
import java.io.File

public open class EngineProjectExtension(project: Project) {

    /**
     *
     */
    public var exposedPackagesToParser: List<String> = mutableListOf()

    /**
     * Whether or not we are running in productionMode. Defaults to false.
     * Responds to the `-Pvaadin.productionMode` property.
     */
    public var productionMode: Boolean = false

    /**
     * The folder where flow will put TS API files for client projects.
     */
    public var generatedTsFolder: File = File(project.projectDir, "frontend/generated")


    /**
     * Defines the output directory for generated non-served resources, such as
     * the token file. Defaults to `build/vaadin-generated` folder.
     *
     * The plugin will automatically register
     * this as an additional resource folder, which should then be picked up by the IDE.
     * That will allow the app to run for example in Intellij with Tomcat.
     * Generating files into build/resources/main wouldn't work since Intellij+Tomcat
     * ignores that folder.
     *
     * The `flow-build-info.json` file is generated here.
     */
    public var resourceOutputDirectory: File = File(project.buildDir, "vaadin-generated")

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


    override fun toString(): String = "VaadinFlowPluginExtension(" +
            "exposedPackagesToParser=$exposedPackagesToParser, " +
            "productionMode=$productionMode, " +
            "generatedTsFolder=$generatedTsFolder, " +
            "resourceOutputDirectory=$resourceOutputDirectory, " +
            "sourceSetName=$sourceSetName, " +
            "nodeCommand=$nodeCommand, " +
            ")"
}
