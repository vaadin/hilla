/**
 *    Copyright 2000-2023 Vaadin Ltd
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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipInputStream
import kotlin.test.expect
import kotlin.test.fail

/**
 * Expects that given task succeeded. If not, fails with an informative exception.
 * @param taskName the name of the task, e.g. `vaadinPrepareNode`
 */
fun BuildResult.expectTaskSucceded(taskName: String) {
    expectTaskOutcome(taskName, TaskOutcome.SUCCESS)
}

/**
 * Expects that given task has [expectedOutcome]. If not, fails with an informative exception.
 * @param taskName the name of the task, e.g. `vaadinPrepareNode` or `web:vaadinBuildFrontend`.
 */
fun BuildResult.expectTaskOutcome(taskName: String, expectedOutcome: TaskOutcome) {
    val task: BuildTask = task(":$taskName")
            ?: fail("Task $taskName was not ran. Executed tasks: ${tasks}. Stdout:\n$output")
    expect(expectedOutcome, "$taskName outcome was ${task.outcome}. Stdout:\n$output") {
        task.outcome
    }
}

/**
 * Expects that given task was not executed. If it was, fails with an informative exception.
 * @param taskName the name of the task, e.g. `vaadinPrepareNode` or `web:vaadinBuildFrontend`.
 */
fun BuildResult.expectTaskNotRan(taskName: String) {
    val task: BuildTask? = task(":$taskName")
    expect(null, "$taskName was not expected to be run. Executed tasks: $tasks. Stdout:\n$output") {
        task
    }
}

/**
 * Finds all files matching given [glob] pattern, for example `libs/ *.war`
 * Always pass in forward slashes as path separators, even on Windows.
 * @param expectedCount expected number of files, defaults to 1.
 */
fun File.find(glob: String, expectedCount: IntRange = 1..1): List<File> {
    val pattern: String = if (OsUtils.isWindows) {
        // replace \ with \\ to avoid collapsing; replace forward slashes in glob with \\
        "glob:$absolutePath".replace("""\""", """\\""") + """\\""" + glob.replace("/", """\\""")
    } else {
        "glob:$absolutePath/$glob"
    }
    val matcher: PathMatcher = FileSystems.getDefault().getPathMatcher(pattern)
    val found: List<File> = absoluteFile.walk()
            .filter { matcher.matches(it.toPath()) }
            .toList()
    if (found.size !in expectedCount) {
        fail("Expected $expectedCount $glob but found ${found.size}: $found . Folder dump: ${absoluteFile.walk().joinToString("\n")}")
    }
    return found
}

/**
 * Converts glob such as `*.jar` into a Regex which matches such files. Always
 * pass in forward slashes as path separators, even on Windows.
 */
private fun String.globToRegex(): Regex =
        Regex(this.replace("?", "[^/]?").replace("*", "[^/]*"))

/**
 * Lists all files in this zip archive, e.g. `META-INF/VAADIN/config/stats.json`.
 * Always returns forward slashes as path separators, even on Windows.
 */
private fun ZipInputStream.fileNameSequence(): Sequence<String> =
        generateSequence { nextEntry?.name }

/**
 * Lists all files in this zip archive, e.g. `META-INF/VAADIN/config/stats.json`.
 */
private fun File.zipListAllFiles(): List<String> =
        ZipInputStream(this.inputStream().buffered()).use { zin: ZipInputStream ->
            zin.fileNameSequence().toList()
        }

/**
 * Expects that given archive contains at least one file matching every glob in the [globs] list.
 * @param archiveProvider returns the zip file to examine.
 */
fun expectArchiveContains(vararg globs: String, archiveProvider: () -> File) {
    val archive: File = archiveProvider()
    val allFiles: List<String> = archive.zipListAllFiles()

    globs.forEach { glob: String ->
        val regex: Regex = glob.globToRegex()
        val someFileMatch: Boolean = allFiles.any { it.matches(regex) }
        expect(true, "No file $glob in $archive, found ${allFiles.joinToString("\n")}") { someFileMatch }
    }
}

/**
 * Expects that given archive doesn't contain any file matching any glob in the [globs] list.
 * @param archiveProvider returns the zip file to examine.
 */
fun expectArchiveDoesntContain(vararg globs: String, archiveProvider: () -> File) {
    val archive: File = archiveProvider()
    val allFiles: List<String> = archive.zipListAllFiles()

    globs.forEach { glob: String ->
        val regex: Regex = glob.globToRegex()
        val someFileMatch: Boolean = allFiles.any { it.matches(regex) }
        expect(false, "Unexpected files $glob found in $archive, found ${allFiles.joinToString("\n")}") { someFileMatch }
    }
}

fun expectArchiveContainsVaadinBundle(archive: File,
                                             isSpringBootJar: Boolean,
                                      compressedExtension: String = "*.br"
                                        ) {
    val isWar: Boolean = archive.name.endsWith(".war", true)
    val isStandaloneJar: Boolean = !isWar && !isSpringBootJar
    val resourcePackaging: String = when {
        isWar -> "WEB-INF/classes/"
        else -> ""
    }
    expectArchiveContains(
            "${resourcePackaging}META-INF/VAADIN/config/flow-build-info.json",
            "${resourcePackaging}META-INF/VAADIN/config/stats.json",
            "${resourcePackaging}META-INF/VAADIN/webapp/VAADIN/build/${compressedExtension}",
            "${resourcePackaging}META-INF/VAADIN/webapp/VAADIN/build/*.js"
    ) { archive }
    if (!isStandaloneJar) {
        val libPrefix: String = if (isSpringBootJar) "BOOT-INF/lib" else "WEB-INF/lib"
        expectArchiveContains("$libPrefix/*.jar") { archive }
    }

    // make sure there is only one flow-build-info.json
    val allFiles: List<String> = archive.zipListAllFiles()
    expect(1, "Multiple flow-build-info.json found: ${allFiles.joinToString("\n")}") {
        allFiles.count { it.contains("flow-build-info.json") }
    }
}

/**
 * Asserts that given archive (jar/war) doesn't contain the Vaadin webpack bundle:
 * the `META-INF/VAADIN/build/` directory.
 */
fun expectArchiveDoesntContainVaadinWebpackBundle(archive: File,
                                                  isSpringBootJar: Boolean) {
    val isWar: Boolean = archive.name.endsWith(".war", true)
    val isStandaloneJar: Boolean = !isWar && !isSpringBootJar
    val resourcePackaging: String = when {
        isWar -> "WEB-INF/classes/"
        isSpringBootJar -> "BOOT-INF/classes/"
        else -> ""
    }
    expectArchiveContains("${resourcePackaging}META-INF/VAADIN/config/flow-build-info.json") { archive }
    expectArchiveDoesntContain("${resourcePackaging}META-INF/VAADIN/config/stats.json",
            "${resourcePackaging}META-INF/VAADIN/webapp/VAADIN/build/*.gz",
            "${resourcePackaging}META-INF/VAADIN/webapp/VAADIN/build/*.js"
    ) { archive }

    if (!isStandaloneJar) {
        val libPrefix: String = if (isSpringBootJar) "BOOT-INF/lib" else "WEB-INF/lib"
        expectArchiveContains("$libPrefix/*.jar") { archive }
    }

    // make sure there is only one flow-build-info.json
    val allFiles: List<String> = archive.zipListAllFiles()
    expect(1, "Multiple flow-build-info.json found: ${allFiles.joinToString("\n")}") {
        allFiles.count { it.contains("flow-build-info.json") }
    }
}

/**
 * Operating system-related utilities.
 */
object OsUtils {
    val osName: String = System.getProperty("os.name")

    /**
     * True if we're running on Windows, false on Linux, Mac and others.
     */
    val isWindows: Boolean get() = osName.startsWith("Windows")
}

fun addConfigurationsToSettingsForUsingPluginFromLocalRepo(testProject: TestProject) {
    testProject.settingsFile.writeText(
        """
           pluginManagement {
              repositories {
                  mavenLocal()
                  maven { url = 'https://maven.vaadin.com/vaadin-prereleases' }
                  gradlePluginPortal()
              }
           }

           rootProject.name = 'junit-hilla-gradle'
        """.trimIndent()
    )
}

/**
 * A testing Gradle project, created in a temporary directory.
 *
 * Used to test the plugin. Contains helpful utility methods to manipulate folders
 * and files in the project.
 */
class TestProject {
    /**
     * The project root dir.
     */
    val dir: File = createTempDir("junit-hilla-gradle-plugin")

    /**
     * The main `build.gradle` file.
     */
    val buildFile: File get() = File(dir, "build.gradle")

    /**
     * The main `settings.gradle` file.
     */
    val settingsFile: File get() = File(dir, "settings.gradle")

    private fun createGradleRunner(): GradleRunner = GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()
        .withDebug(true) // use --debug to catch ReflectionsException: https://github.com/vaadin/vaadin-gradle-plugin/issues/99
        .forwardOutput()   // a must, otherwise ./gradlew check freezes on windows!
        .withGradleVersion("8.3")

    override fun toString(): String = "TestProject(dir=$dir)"

    /**
     * Deletes the project directory and nukes all project files.
     */
    fun delete() {
        // don't throw an exception if the folder fails to be deleted. The folder
        // is temporary anyway, and Windows tends to randomly fail with
        // java.nio.file.FileSystemException: C:\Users\RUNNER~1\AppData\Local\Temp\junit-vaadin-gradle-plugin8993583259614232822.tmp\lib\build\libs\lib.jar: The process cannot access the file because it is being used by another process.
        deleteDirectory(directory = dir.toPath())
    }

    private fun deleteDirectory(directory: Path) {
        Files.walkFileTree(directory, setOf(), Integer.MAX_VALUE, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (exc == null) {
                    Files.delete(dir)
                } else {
                    println("Failed to delete temp project folder $dir, error: ${exc.message}")
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * Creates a new [folder] in the project folder. Does nothing if the folder
     * already exists.
     */
    fun newFolder(folder: String): File {
        val newFolder = Files.createDirectories(File(dir.absoluteFile, folder).toPath())
        return newFolder.toFile()
    }

    /**
     * Runs build on this project; a `build.gradle` [buildFile] is expected
     * to be located there.
     *
     * The function by default checks that all tasks have succeeded; if not, throws an informative exception.
     * You can suppress this functionality by setting [checkTasksSuccessful] to false.
     */
    fun build(vararg args: String, checkTasksSuccessful: Boolean = true, debug: Boolean = false): BuildResult {
        expect(true, "$buildFile doesn't exist, can't run build") { buildFile.exists() }

        println("$dir/./gradlew ${args.joinToString(" ")}")
        val result: BuildResult = createGradleRunner()
            .withArguments(args.toList() + "--stacktrace" + "--info")
            .build()

        if (checkTasksSuccessful) {
            for (arg: String in args) {
                val isTask: Boolean = !arg.startsWith("-")
                if (isTask) {
                    result.expectTaskSucceded(arg)
                }
            }
        }
        return result
    }

    /**
     * Runs and fails the build on this project;
     */
    fun buildAndFail(vararg args: String): BuildResult {
        println("$dir/./gradlew ${args.joinToString(" ")}")
        return createGradleRunner()
                .withArguments(args.toList() + "--stacktrace" + "--info")
                .buildAndFail()
    }

    /**
     * Creates a file in the temporary test project.
     */
    fun newFile(fileNameWithPath: String, contents: String = ""): File {
        val file = File(dir, fileNameWithPath)
        Files.createDirectories(file.parentFile.toPath())
        file.writeText(contents)
        return file
    }

    /**
     * Looks up a [folder] in the project and returns it.
     */
    fun folder(folder: String): File {
        val dir = File(dir, folder)
        check(dir.exists()) { "$dir doesn't exist" }
        check(dir.isDirectory) { "$dir isn't a directory" }
        return dir
    }

    /**
     * Returns the WAR file built. Fails if there's no war file in `build/libs`.
     */
    val builtWar: File get() {
        val war = folder("build/libs").find("*.war").first()
        expect(true, "$war is missing") { war.isFile }
        return war
    }

    val builtJar: File get() {
        val jar: File = folder("build/libs").find("*.jar").first()
        expect(true, "$jar is missing") { jar.isFile }
        return jar
    }
}

/**
 * Similar to [File.deleteRecursively] but throws informative [IOException] instead of
 * just returning false on error. uses Java 8 [Files.deleteIfExists] to delete files and folders.
 */
fun Path.deleteRecursively() {
    toFile().walkBottomUp().forEach { Files.deleteIfExists(it.toPath()) }
}
