package dev.hilla.gradle.plugin

import dev.hilla.plugin.base.InitFileExtractor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.IOException

public open class EngineInitAppTask : DefaultTask() {

    init {
        group = "Hilla"
        description = "Task for scaffolding and initializing a Hilla application"
    }

    @TaskAction
    public fun hillaInitApp() {

        val extractor = InitFileExtractor(
            project.projectDir.toPath()
        )

        try {
            extractor.execute()
        } catch (e: IOException) {
            throw GradleException("Execution failed", e)
        }
    }
}
