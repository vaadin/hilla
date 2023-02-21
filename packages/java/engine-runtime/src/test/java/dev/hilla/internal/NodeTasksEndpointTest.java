package dev.hilla.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.NodeTasks;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;

public class NodeTasksEndpointTest extends TaskTest {
    private Options options;

    @Endpoint
    public static class ConnectEndpointsForTesting {
    }

    @BeforeEach
    public void setUp() throws IOException {
        File src = Files
                .createDirectories(getTemporaryDirectory().resolve("src"))
                .toFile();
        File json = getTemporaryDirectory().toAbsolutePath()
                .resolve("api-file.json").toFile();

        Lookup mockLookup = Mockito.mock(Lookup.class);
        Mockito.doReturn(new EndpointGeneratorTaskFactoryImpl())
                .when(mockLookup).lookup(EndpointGeneratorTaskFactory.class);
        Mockito.doReturn(new DefaultClassFinder(
                Collections.singleton(ConnectEndpointsForTesting.class)))
                .when(mockLookup).lookup(ClassFinder.class);

        options = new Options(mockLookup, getTemporaryDirectory().toFile())
                .withBuildDirectory(getBuildDirectory())
                .enablePackagesUpdate(false).enableImportsUpdate(false)
                .withEmbeddableWebComponents(false)
                .withEndpointSourceFolder(src)
                .withEndpointGeneratedOpenAPIFile(json)
                .withFrontendGeneratedFolder(
                        getTemporaryDirectory().resolve("api").toFile())
                .withJarFrontendResourcesFolder(getTemporaryDirectory()
                        .resolve("jar-resources").toFile());
    }

    @Test
    public void should_GenerateEndpointFiles() throws Exception {
        new NodeTasks(options).execute();

        Arrays.asList(
                // enableClientSide
                "frontend/index.html", "frontend/generated/index.ts",
                // withConnectJavaSourceFolder and
                // withConnectGeneratedOpenApiJson
                "build/classes/dev/hilla/openapi.json",
                // withConnectClientTsApiFolder
                "api/connect-client.default.ts", "api/MyEndpoint.ts")
                .forEach(name -> assertTrue(
                        new File(getTemporaryDirectory().toFile(), name)
                                .exists(),
                        name + " not created."));
    }

}
