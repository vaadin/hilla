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

import com.vaadin.gradle.VaadinFlowPluginExtension
import com.vaadin.hilla.engine.EngineConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Task that generates the configuration json which is needed
 * for next task of generating the endpoints and model classes.
 */
public abstract class EngineConfigureTask : DefaultTask() {

    init {
        group = "Vaadin"
        description = "Hilla Configure Task"
    }

    internal fun configure(project: Project) {
        val engineConfig = HillaPlugin.createEngineConfiguration(
            project,
            VaadinFlowPluginExtension.get(project)
        )
        engineConfigurationSettings.set(engineConfig.toInputs())
    }

    @get:Internal
    internal abstract val engineConfigurationSettings: Property<EngineConfigurationSettings>

    @TaskAction
    public fun engineConfigure() {
        EngineConfiguration.setDefault(
            engineConfigurationSettings.get().toEngineConfiguration()
        )
    }
}
