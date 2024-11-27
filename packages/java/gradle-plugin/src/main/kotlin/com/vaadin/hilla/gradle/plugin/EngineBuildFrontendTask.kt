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
import com.vaadin.hilla.engine.EngineConfiguration

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

/**
 * Extend the VaadinBuildFrontendTask so that frontend files are not cleaned after build.
 */
public open class EngineBuildFrontendTask : com.vaadin.gradle.VaadinBuildFrontendTask() {
    @Input
    public val classpathElements: List<String> = project.configurations.getByName("compileClasspath").files.map { it.absolutePath }

    @Input
    public val groupId: String = project.group.toString()

    @Input
    public val artifactId: String = project.name

    @InputFile
    public val buildDir: File = project.buildDir

    @Input
    @Optional
    public var mainClass: String? = project.findProperty("mainClass") as String?

    @TaskAction
    public fun exec() {
        val config = PluginEffectiveConfiguration.get(project)
        val engineConfiguration = EngineConfiguration.Builder()
            .baseDir(config.npmFolder.get().toPath())
            .buildDir(buildDir.toPath())
            .outputDir(config.generatedTsFolder.get().toPath())
            .groupId(groupId)
            .artifactId(artifactId)
            .classpath(classpathElements)
            .mainClass(mainClass)
            .create()

        EngineConfiguration.setDefault(engineConfiguration)

        super.vaadinBuildFrontend()
    }
}
