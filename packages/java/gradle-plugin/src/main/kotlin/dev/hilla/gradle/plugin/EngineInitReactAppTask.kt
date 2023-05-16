package dev.hilla.gradle.plugin

import dev.hilla.plugin.base.InitFileExtractor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.IOException

public open class EngineInitReactAppTask : DefaultTask() {

    init {
        group = "Hilla"
        description = "Task for scaffolding and initializing a Hill-React-App"
    }

    @TaskAction
    public fun hillaInitReactApp() {

        val skeletonUrl = "https://github.com/vaadin/skeleton-starter-hilla-react/archive/refs/heads/v2.1.zip"
        val items = listOf(
            "package.json", "package-lock.json", "vite.config.ts",
            "tsconfig.json", "types.d.ts",
            "frontend/App.tsx", "frontend/index.ts", "frontend/routes.tsx",
            "frontend/views/MainView.tsx",
            "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java"
        )
        val extractor = InitFileExtractor(
            skeletonUrl, items,
            project.projectDir.toPath()
        )

        try {
            extractor.execute()
        } catch (e: IOException) {
            throw GradleException("Execution failed", e)
        }
    }
}
