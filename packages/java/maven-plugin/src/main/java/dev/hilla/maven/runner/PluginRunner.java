package dev.hilla.maven.runner;

import dev.hilla.parser.utils.OpenAPIPrinter;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PluginRunner {
    private static final Logger logger = LoggerFactory
            .getLogger(PluginRunner.class);
    private static final String[] openAPIFileRelativePath = {
            "generated-resources", "openapi.json" };

    private final PluginConfiguration conf;

    public PluginRunner(PluginConfiguration conf) {
        this.conf = conf;
    }

    public void execute()
            throws PluginException, GeneratorUnavailableException {
        var result = parseJavaCode();
        var openAPI = saveOpenAPI(result);
        generateTypeScriptCode(openAPI);
    }

    private void generateTypeScriptCode(String openAPI)
            throws PluginException, GeneratorUnavailableException {
        try {
            var executor = new GeneratorProcessor(conf.getBaseDir())
                    .input(openAPI);

            var generator = conf.getGenerator();
            generator.getOutputDir().ifPresent(executor::outputDir);
            generator.getPlugins().ifPresent(executor::plugins);

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new PluginException("TS code generation failed", e);
        }
    }

    private String saveOpenAPI(OpenAPI openAPI) throws PluginException {
        try {
            var openAPIFile = Paths.get(conf.getBuildDir(),
                    openAPIFileRelativePath);

            logger.debug("Saving OpenAPI file to " + openAPIFile);

            Files.createDirectories(openAPIFile.getParent());
            var openAPIString = new OpenAPIPrinter().pretty()
                    .writeAsString(openAPI);
            Files.write(openAPIFile, openAPIString.getBytes());

            logger.debug("OpenAPI file saved");

            return openAPIString;
        } catch (IOException e) {
            throw new PluginException("Saving OpenAPI file failed", e);
        }
    }

    private OpenAPI parseJavaCode() throws PluginException {
        try {
            var executor = new ParserProcessor(conf.getBaseDir(),
                    conf.getClassPath());
            var parser = conf.getParser();

            parser.getClassPath().ifPresent(executor::classPath);
            parser.getEndpointAnnotation()
                    .ifPresent(executor::endpointAnnotation);
            parser.getEndpointExposedAnnotation()
                    .ifPresent(executor::endpointExposedAnnotation);
            parser.getPlugins().ifPresent(executor::plugins);
            parser.getOpenAPIPath().ifPresent(executor::openAPIBase);

            return executor.process();
        } catch (ParserException e) {
            throw new PluginException("Java code parsing failed", e);
        }
    }
}
