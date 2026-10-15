/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
            val isProductionMode = vaadinExtension.productionMode.getOrElse(false);
            val parserProcessor = ParserProcessor(conf, classLoader, isProductionMode)
            val generatorProcessor = GeneratorProcessor(conf, extension.nodeCommand, isProductionMode)

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
