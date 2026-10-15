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
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.nio.file.Files
import java.nio.file.Path

import com.vaadin.hilla.engine.*

/**
 * Task that generates the configuration json which is needed
 * for next task of generating the endpoints and model classes.
 */
public open class EngineConfigureTask : DefaultTask() {

    private val sourceSets: SourceSetContainer by lazy {
        project.extensions.getByType(SourceSetContainer::class.java)
    }

    init {
        group = "Vaadin"
        description = "Hilla Configure Task"
    }

    private val legacyProjectFrontendPath = "./frontend"

    @TaskAction
    public fun engineConfigure() {
        val extension: EngineProjectExtension = EngineProjectExtension.get(project)
        logger.info("Running the engineConfigure task with effective Hilla configuration $extension")
        val vaadinExtension = VaadinFlowPluginExtension.get(project)
        logger.info("Running the engineConfigure task with effective Vaadin configuration $extension")

        val generator = GeneratorConfiguration()
        val parser = ParserConfiguration()
        if (extension.exposedPackagesToParser.isNotEmpty()) {
            parser.setPackages(extension.exposedPackagesToParser)
        }

        val projectBuildDir = project.layout.buildDirectory.get().asFile.toPath()
        val projectClassesDir = projectBuildDir.resolve("classes")
        val classPathElements = (sourceSets.getByName(vaadinExtension.sourceSetName.get()) as SourceSet)
            .runtimeClasspath.elements.get().stream().map { it.toString() }.toList()

        var generatedTsFolder = vaadinExtension.generatedTsFolder.get().toPath()
        val legacyFrontendFolder = project.projectDir.toPath().resolve(legacyProjectFrontendPath)
        if (Files.exists(legacyFrontendFolder)) {
            generatedTsFolder = legacyFrontendFolder.resolve("generated")
        }

        val conf = EngineConfiguration.Builder(project.projectDir.toPath())
            .classPath(classPathElements)
            .outputDir(generatedTsFolder)
            .generator(generator)
            .parser(parser)
            .buildDir(vaadinExtension.projectBuildDir.get())
            .classesDir(projectClassesDir)
            .create()

        val configDir: Path = project.projectDir.toPath().resolve(projectBuildDir)
        Files.createDirectories(configDir)
        conf.store(
          configDir
            .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME)
            .toFile()
        )
    }
}
