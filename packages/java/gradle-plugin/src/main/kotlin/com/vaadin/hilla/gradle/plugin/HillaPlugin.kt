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

import com.vaadin.gradle.VaadinPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy

/**
 * The main class of the Hilla Gradle Plugin
 */
public class HillaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // we need Java Plugin conventions so that we can ensure the order of tasks
        project.pluginManager.apply(JavaPlugin::class.java)

        // we apply Vaadin (flow) plugin so that the users do not need to add it themselves
        // to leverage from vaadinPrepareFrontend and vaadinBuildFrontend:
        project.pluginManager.apply(VaadinPlugin::class.java)

        project.tasks.replace("vaadinBuildFrontend", EngineBuildFrontendTask::class.java)

        val extensionName = "hilla"
        project.extensions.create(extensionName, EngineProjectExtension::class.java, project)

        project.tasks.apply {
            register("hillaConfigure", EngineConfigureTask::class.java)
            register("hillaGenerate", EngineGenerateTask::class.java)
        }

        project.tasks.named("processResources") {
            val copyTask = it as? Copy
            if (copyTask != null) {
                copyTask.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }
    }
}
