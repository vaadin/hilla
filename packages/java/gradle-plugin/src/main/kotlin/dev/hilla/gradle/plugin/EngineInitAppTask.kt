package dev.hilla.gradle.plugin

import dev.hilla.plugin.base.HillaAppInitUtility
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
        try {

            val dependencyArtifactIds = project.configurations.getByName("implementation")
                .dependencies.map { it.name }.stream().toList()
            HillaAppInitUtility.scaffold(project.projectDir.toPath(), dependencyArtifactIds)

        } catch (e: IOException) {
            throw GradleException("Execution failed", e)
        }
    }
}
