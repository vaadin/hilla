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
import com.vaadin.hilla.BrowserCallable
import com.vaadin.hilla.Endpoint
import com.vaadin.hilla.EndpointExposed
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import com.vaadin.hilla.engine.*
import org.gradle.api.tasks.bundling.Jar
import java.util.stream.Stream

/**
 * Task that generates the configuration json which is needed
 * for next task of generating the endpoints and model classes.
 */
public open class EngineConfigureTask : DefaultTask() {

    init {
        group = "Vaadin"
        description = "Hilla Configure Task"

        project.tasks.withType(Jar::class.java) { task: Jar ->
            task.mustRunAfter("vaadinBuildFrontend")
        }

    }

    @TaskAction
    public fun engineConfigure() {
        val vaadinExtension = VaadinFlowPluginExtension.get(project)
        val sourceSets: SourceSetContainer by lazy {
            project.extensions.getByType(SourceSetContainer::class.java)
        }
        val sourceSet = sourceSets.getByName(vaadinExtension.sourceSetName.get()) as SourceSet;
        val classpathElements = sourceSet.runtimeClasspath.elements.get().stream().map { it.toString() }
        val pluginClasspath = project.buildscript.configurations.getByName("classpath")
            .resolve().stream().map { it.toString() }.filter { it.contains("-loader-tools") }
        val classpath = Stream.concat(pluginClasspath, classpathElements).distinct().toList()

        val engineConfiguration = EngineConfiguration.Builder()
            .baseDir(vaadinExtension.npmFolder.get().toPath())
            .buildDir(project.buildDir.toPath())
            .classesDir(sourceSet.output.classesDirs.singleFile.toPath())
            .outputDir(vaadinExtension.generatedTsFolder.get().toPath())
            .groupId(project.group.toString().takeIf { it.isNotEmpty() } ?: "unspecified")
            .artifactId(project.name)
            .classpath(classpath)
            .mainClass(project.findProperty("mainClass") as String?)
            .productionMode(vaadinExtension.productionMode.getOrElse(false))
            .endpointAnnotations(Endpoint::class.java, BrowserCallable::class.java)
            .endpointExposedAnnotations(EndpointExposed::class.java)
            .create()

        engineConfiguration.parser.endpointAnnotations = listOf(Endpoint::class.java, BrowserCallable::class.java)
        engineConfiguration.parser.endpointExposedAnnotations = listOf(EndpointExposed::class.java)

        EngineConfiguration.setDefault(engineConfiguration)
    }
}
