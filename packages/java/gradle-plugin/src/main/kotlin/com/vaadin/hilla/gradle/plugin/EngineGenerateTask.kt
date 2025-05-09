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

import java.io.IOException
import com.vaadin.flow.gradle.PluginEffectiveConfiguration
import com.vaadin.flow.gradle.VaadinFlowPluginExtension
import com.vaadin.hilla.engine.GeneratorException
import com.vaadin.hilla.engine.GeneratorProcessor
import com.vaadin.hilla.engine.ParserException
import com.vaadin.hilla.engine.ParserProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

/**
 * Task that generates the endpoints.ts and model TS classes
 * needed for calling the backend in a typesafe manner.
 */
public abstract class EngineGenerateTask : DefaultTask() {

    init {
        group = "Vaadin"
        description = "Hilla Generate Task"

        // we need the compiled classes:
        dependsOn("classes")

        // Make sure to run this task before the `war`/`jar` tasks, so that
        // generated endpoints and models will end up packaged in the war/jar archive.
        // The inclusion rule itself is configured in the HillaPlugin class.
        project.tasks.withType(Jar::class.java) { task: Jar ->
            task.mustRunAfter("generate")
        }
    }

    internal fun configure(project: Project) {
        groupId.set(project.group.toString())
        artifactId.set(project.name)
        mainClass.set(project.findProperty("mainClass") as String?)
        val engineConfig = HillaPlugin.createEngineConfiguration(
            project,
            VaadinFlowPluginExtension.get(project)
        )
        engineConfigurationSettings.set(engineConfig.toInputs())
        effectiveConfig.set(PluginEffectiveConfiguration.get(project))
        classpath.from(engineConfig.classpath.map { it.toFile() })

        openApiFile.set(engineConfig.openAPIFile.toFile())
        outputDir.set(engineConfig.outputDir.toFile())
    }

    @get:Internal
    internal abstract val effectiveConfig: Property<PluginEffectiveConfiguration>

    @get:Input
    internal abstract val groupId: Property<String>

    @get:Input
    internal abstract val artifactId: Property<String>

    @get:Optional
    @get:Input
    internal abstract val mainClass: Property<String?>

    @get:Input
    internal abstract val engineConfigurationSettings: Property<EngineConfigurationSettings>

    @get:CompileClasspath
    internal abstract val classpath : ConfigurableFileCollection

    @get:Optional
    @get:OutputFile
    internal abstract val openApiFile: RegularFileProperty

    @get:OutputDirectory
    internal abstract val outputDir: DirectoryProperty

    @TaskAction
    public fun engineGenerate() {

        logger.info("Running the engineGenerate task with effective Vaadin configuration ${effectiveConfig.get()}")

        try {
            val conf = engineConfigurationSettings.get().toEngineConfiguration()

            val parserProcessor = ParserProcessor(conf)
            val generatorProcessor = GeneratorProcessor(conf)

            val endpoints = conf.browserCallableFinder.findBrowserCallables();
            parserProcessor.process(endpoints)
            generatorProcessor.process()
        } catch (e: IOException) {
            throw GradleException("Endpoint collection failed", e)
        } catch (e: InterruptedException) {
            throw GradleException("Endpoint collection failed", e)
        } catch (e: GeneratorException) {
            throw GradleException("Execution failed", e)
        } catch (e: ParserException) {
            throw GradleException("Execution failed", e)
        }
    }
}
