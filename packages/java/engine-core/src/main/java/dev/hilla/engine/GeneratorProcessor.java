package dev.hilla.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.NpmPackage;

@NpmPackage(value = "@hilla/generator-typescript-core", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-push", version = "2.0.0-beta1")
public final class GeneratorProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorProcessor.class);
    private final Path baseDir;
    private final String nodeCommand;
    private final Path openAPIFile;
    private final Path outputDirectory;
    private final GeneratorConfiguration.PluginsProcessor pluginsProcessor = new GeneratorConfiguration.PluginsProcessor();

    public GeneratorProcessor(EngineConfiguration conf, String nodeCommand) {
        this.baseDir = conf.getBaseDir();
        this.openAPIFile = conf.getOpenAPIFile();
        this.outputDirectory = conf.getOutputDirectory();
        this.nodeCommand = nodeCommand;
        applyConfiguration(conf.getGenerator());
    }

    public void process() throws GeneratorException {
        var runner = new GeneratorShellRunner(baseDir.toFile(), nodeCommand);
        prepareOutputDir(runner);
        preparePlugins(runner);
        prepareVerbose(runner);
        try {
            var input = Files.readString(openAPIFile);
            runner.run(input);
        } catch (IOException | InterruptedException e) {
            throw new GeneratorException("Unable to generate code", e);
        }
    }

    private GeneratorProcessor applyConfiguration(
            GeneratorConfiguration generatorConfiguration) {
        if (generatorConfiguration == null) {
            return this;
        }

        generatorConfiguration.getPlugins().ifPresent(this::applyPlugins);
        return this;
    }

    private void applyPlugins(@Nonnull GeneratorConfiguration.Plugins plugins) {
        pluginsProcessor.setConfig(plugins);
    }

    private void prepareOutputDir(GeneratorShellRunner runner) {
        var result = outputDirectory.isAbsolute() ? outputDirectory
                : baseDir.resolve(outputDirectory);
        runner.add("-o", result.toString());
    }

    private void preparePlugins(GeneratorShellRunner runner) {
        pluginsProcessor.process().stream()
                .map(GeneratorConfiguration.Plugin::getPath).distinct()
                .forEachOrdered(path -> runner.add("-p", path));
    }

    private void prepareVerbose(GeneratorShellRunner runner) {
        if (logger.isDebugEnabled()) {
            runner.add("-v");
        }
    }
}
