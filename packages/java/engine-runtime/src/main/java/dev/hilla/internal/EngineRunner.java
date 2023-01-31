package dev.hilla.internal;

import dev.hilla.parser.utils.OpenAPIPrinter;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EngineRunner {
    private static final Logger logger = LoggerFactory
            .getLogger(EngineRunner.class);
    private static final String[] openAPIFileRelativePath = {
            "generated-resources", "openapi.json" };

    private final EngineConfiguration conf;

    public EngineRunner(EngineConfiguration conf) {
        this.conf = conf;
    }

    public void execute()
            throws EngineException, GeneratorUnavailableException {
        var result = parseJavaCode();
        var openAPI = saveOpenAPI(result);
        generateTypeScriptCode(openAPI);
    }

    private void generateTypeScriptCode(String openAPI)
            throws EngineException, GeneratorUnavailableException {
        try {
            var executor = new GeneratorProcessor(conf.getBaseDir())
                    .input(openAPI);

            var generator = conf.getGenerator();
            generator.getPlugins().ifPresent(executor::plugins);

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new EngineException("TS code generation failed", e);
        }
    }

    private String saveOpenAPI(OpenAPI openAPI) throws EngineException {
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
            throw new EngineException("Saving OpenAPI file failed", e);
        }
    }

    private OpenAPI parseJavaCode() throws EngineException {
        try {
            var processor = new ParserProcessor(conf.getBaseDir(),
                    conf.getClassPath());
            var parser = conf.getParser();

            parser.getClassPath().ifPresent(processor::classPath);
            parser.getEndpointAnnotation()
                    .ifPresent(processor::endpointAnnotation);
            parser.getEndpointExposedAnnotation()
                    .ifPresent(processor::endpointExposedAnnotation);
            parser.getPlugins().ifPresent(processor::plugins);
            parser.getOpenAPIPath().ifPresent(processor::openAPIBase);

            return processor.process();
        } catch (ParserException e) {
            throw new EngineException("Java code parsing failed", e);
        }
    }
}
