package dev.hilla.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

public final class GeneratorProcessor {
    private final Path baseDir;

    private final Path openAPIFile;

    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorProcessor.class);
    private final Path outputDirectory;
    private final GeneratorConfiguration.PluginsProcessor pluginsProcessor = new GeneratorConfiguration.PluginsProcessor();

    public GeneratorProcessor(EngineConfiguration conf) {
        this.baseDir = conf.getBaseDir();
        this.openAPIFile = conf.getOpenAPIFile();
        this.outputDirectory = conf.getOutputDirectory();
        applyConfiguration(conf.getGenerator());
    }

    public void process() throws GeneratorException {
        var runner = new GeneratorShellRunner(baseDir.toFile());
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

    private void prepareOutputDir(GeneratorShellRunner runner) {
        var result = outputDirectory.isAbsolute() ? outputDirectory
                : baseDir.resolve(outputDirectory);
        runner.add("-o", result.toString());
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
