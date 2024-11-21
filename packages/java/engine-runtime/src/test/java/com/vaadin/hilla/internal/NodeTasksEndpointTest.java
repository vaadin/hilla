package com.vaadin.hilla.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.NodeTasks;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointController;
import com.vaadin.hilla.engine.EngineConfiguration;

public class NodeTasksEndpointTest extends TaskTest {
    private Options options;

    public static class ConnectEndpointsForTesting {
    }

    @Endpoint
    public class MyEndpoint {
        public void foo(String bar) {
        }

        public String bar(String baz) {
            return baz;
        }
    }

    @Endpoint(value = "CustomEndpointName")
    public class CustomEndpoint {
        public void foo(String bar) {
        }

        public String bar(String baz) {
            return baz;
        }
    }

    @Endpoint("WithoutValueEqual")
    public class EndpointNoValue {
        public void foo(String bar) {
        }

        public String bar(String baz) {
            return baz;
        }
    }

    @BeforeEach
    public void setUp()
            throws IOException, NoSuchFieldException, IllegalAccessException {
        Lookup mockLookup = Mockito.mock(Lookup.class);
        Mockito.doReturn(new EndpointGeneratorTaskFactoryImpl())
                .when(mockLookup).lookup(EndpointGeneratorTaskFactory.class);
        Mockito.doReturn(new DefaultClassFinder(Set.of(
                // Required to bypass Hilla check in
                // com.vaadin.flow.internal.hilla.EndpointRequestUtil
                // .isHillaAvailable(com.vaadin.flow.server.frontend.scanner.ClassFinder)
                EndpointController.class, ConnectEndpointsForTesting.class)))
                .when(mockLookup).lookup(ClassFinder.class);

        var mockApplicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.doReturn(Map.of("MyEndpoint", new MyEndpoint(),
                "CustomEndpointName", CustomEndpoint.class, "WithoutValueEqual",
                EndpointNoValue.class)).when(mockApplicationContext)
                .getBeansWithAnnotation(Endpoint.class);
        Mockito.doReturn(Map.of()).when(mockApplicationContext)
                .getBeansWithAnnotation(BrowserCallable.class);
        var applicationContextField = ApplicationContextProvider.class
                .getDeclaredField("applicationContext");
        applicationContextField.setAccessible(true);
        applicationContextField.set(null, mockApplicationContext);

        options = new Options(mockLookup, getTemporaryDirectory().toFile())
                .withFrontendDirectory(getTemporaryDirectory()
                        .resolve(getFrontendDirectory()).toFile())
                .withProductionMode(false)
                .withBuildDirectory(getBuildDirectory())
                .withFrontendHotdeploy(true).withRunNpmInstall(false)
                .enablePackagesUpdate(false).enableImportsUpdate(false)
                .withEmbeddableWebComponents(false)
                .withFrontendGeneratedFolder(
                        getTemporaryDirectory().resolve("api").toFile())
                .withJarFrontendResourcesFolder(getTemporaryDirectory()
                        .resolve("jar-resources").toFile());

        EngineConfiguration.getDefault().setBuildDir(getBuildDirectory());

        createIndexFile();
    }

    @Test
    public void should_NotGenerateEndpointFiles() throws Exception {
        new NodeTasks(options).execute();
        assertEndpointFiles(false);
    }

    @Test
    public void should_GenerateEndpointFilesInDevBuildTask() throws Exception {
        options = options.withBundleBuild(true);

        new NodeTasks(options).execute();
        assertEndpointFiles(true);
    }

    @Test
    public void should_GenerateEndpointFilesInProductionBuildTask()
            throws Exception {
        options = options.withProductionMode(true);
        EngineConfiguration.getDefault()
                .setOfflineEndpointProvider(() -> List.of(MyEndpoint.class));

        new NodeTasks(options).execute();
        EngineConfiguration.getDefault().setOfflineEndpointProvider(null);
        assertEndpointFilesInProductionMode(true);
    }

    @Test
    public void should_GenerateEndpointFilesInDevServerTask() throws Exception {
        options = options.withRunNpmInstall(true);
        new NodeTasks(options).execute();
        assertEndpointFiles(true);
    }

    private void assertEndpointFiles(boolean shouldExist) {
        Arrays.asList("target/hilla-openapi.json",
                "api/connect-client.default.ts", "api/MyEndpoint.ts")
                .forEach(name -> assertEquals(shouldExist,
                        new File(getTemporaryDirectory().toFile(), name)
                                .exists(),
                        name + " should " + (shouldExist ? "" : "not ")
                                + "be created"));
    }

    private void assertEndpointFilesInProductionMode(boolean shouldExist) {
        Arrays.asList("target/classes/hilla-openapi.json",
                "api/connect-client.default.ts", "api/MyEndpoint.ts")
                .forEach(name -> assertEquals(shouldExist,
                        new File(getTemporaryDirectory().toFile(), name)
                                .exists(),
                        name + " should " + (shouldExist ? "" : "not ")
                                + "be created"));
    }

    private void createIndexFile() throws IOException {
        String indexContent = """
                import { Router } from '@vaadin/router';
                const router = new Router(document.querySelector('#outlet'));
                router.setRoutes([ { path: '', component: 'hilla-view' } ]);
                """;
        File indexFile = new File(options.getFrontendDirectory(), "index.ts");
        FileUtils.forceMkdirParent(indexFile);
        FileUtils.writeStringToFile(indexFile, indexContent,
                StandardCharsets.UTF_8);
    }
}
