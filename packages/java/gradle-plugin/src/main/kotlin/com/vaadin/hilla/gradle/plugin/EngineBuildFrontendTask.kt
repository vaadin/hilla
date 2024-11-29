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

import com.vaadin.gradle.PluginEffectiveConfiguration
import com.vaadin.gradle.VaadinFlowPluginExtension
import com.vaadin.hilla.engine.EngineConfiguration

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

/**
 * Extend the VaadinBuildFrontendTask so that frontend files are not cleaned after build.
 */
public open class EngineBuildFrontendTask : com.vaadin.gradle.VaadinBuildFrontendTask() {
    @TaskAction
    public fun exec() {
        val vaadinExtension = VaadinFlowPluginExtension.get(project)
        val sourceSets: SourceSetContainer by lazy {
            project.extensions.getByType(SourceSetContainer::class.java)
        }
        val classpathElements = (sourceSets.getByName(vaadinExtension.sourceSetName.get()) as SourceSet)
            .runtimeClasspath.elements.get().stream().map { it.toString() }.toList()
        val config = PluginEffectiveConfiguration.get(project)
        val engineConfiguration = EngineConfiguration.Builder()
            .baseDir(config.npmFolder.get().toPath())
            .buildDir(project.buildDir.toPath())
            .outputDir(config.generatedTsFolder.get().toPath())
            .groupId(project.group.toString())
            .artifactId(project.name)
            .classpath(classpathElements)
            .mainClass(project.findProperty("mainClass") as String?)
            .productionMode(vaadinExtension.productionMode.getOrElse(false))
            .create()

        EngineConfiguration.setDefault(engineConfiguration)

        super.vaadinBuildFrontend()
    }
}
