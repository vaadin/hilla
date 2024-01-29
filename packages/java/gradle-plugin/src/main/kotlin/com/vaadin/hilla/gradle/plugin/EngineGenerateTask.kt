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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*

import com.vaadin.hilla.engine.*
/**
 * Task that generates the endpoints.ts and model TS classes
 * needed for calling the backend in a typesafe manner.
 */
public open class EngineGenerateTask : DefaultTask() {
    init {
        group = "Vaadin"
        description = "Hilla Generate Task"

        // we need the build/hilla-engine-configuration.json and the compiled classes:
        dependsOn("classes", "hillaConfigure")

        // Make sure to run this task before the `war`/`jar` tasks, so that
        // generated endpoints and models will end up packaged in the war/jar archive.
        // The inclusion rule itself is configured in the HillaPlugin class.
        project.tasks.withType(Jar::class.java) { task: Jar ->
            task.mustRunAfter("generate")
        }
    }

    @TaskAction
    public fun engineGenerate() {
        val extension: EngineProjectExtension = EngineProjectExtension.get(project)
        logger.info("Running the engineGenerate task with effective Hilla configuration $extension")
        val vaadinExtension = VaadinFlowPluginExtension.get(project)
        logger.info("Running the engineGenerate task with effective Vaadin configuration $extension")

        val baseDir: Path = project.projectDir.toPath()
        val buildDir: Path = baseDir.resolve(vaadinExtension.projectBuildDir.get())

        try {
            val conf: EngineConfiguration = Objects.requireNonNull(
                EngineConfiguration.loadDirectory(buildDir))

            val urls = conf.classPath
                .stream().map<URL> { classPathItem: Path ->
                    classPathItem.toUri().toURL()
                }
                .toList()

            val classLoader = URLClassLoader(
                urls.toTypedArray(),
                javaClass.classLoader
            )
            val parserProcessor = ParserProcessor(conf, classLoader)
            val generatorProcessor = GeneratorProcessor(conf, extension.nodeCommand)

            parserProcessor.process()
            generatorProcessor.process()

        } catch (e: IOException) {
            throw GradleException("Loading saved configuration failed", e)
        } catch (e: GeneratorException) {
            throw GradleException("Execution failed", e)
        } catch (e: ParserException) {
            throw GradleException("Execution failed", e)
        }
    }
}
