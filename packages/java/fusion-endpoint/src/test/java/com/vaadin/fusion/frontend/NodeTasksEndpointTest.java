package com.vaadin.fusion.frontend;

import static com.vaadin.flow.server.Constants.TARGET;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_GENERATED_DIR;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import com.vaadin.flow.di.Lookup;
import com.vaadin.fusion.Endpoint;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.NodeTasks.Builder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class NodeTasksEndpointTest {

    @Endpoint
    public static class ConnectEndpointsForTesting {
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    String userDir;

    @Before
    public void setup() {
        userDir = temporaryFolder.getRoot().getAbsolutePath();
        System.setProperty("user.dir", userDir);
        System.clearProperty(PARAM_FRONTEND_DIR);
        System.clearProperty(PARAM_GENERATED_DIR);
    }

    @Test
    public void should_Generate_Connect_Files() throws Exception {
        File src = new File(
                getClass().getClassLoader().getResource("java").getFile());
        File dir = new File(userDir);
        File json = new File(dir, "api-file.json");

        Lookup mockLookup = Mockito.mock(Lookup.class);
        Mockito.doReturn(new EndpointGeneratorTaskFactoryImpl())
                .when(mockLookup).lookup(EndpointGeneratorTaskFactory.class);
        Mockito.doReturn(new DefaultClassFinder(
                Collections.singleton(ConnectEndpointsForTesting.class)))
                .when(mockLookup).lookup(ClassFinder.class);
        Builder builder = new Builder(mockLookup, dir, TARGET)
                .enablePackagesUpdate(false).enableImportsUpdate(false)
                .withEmbeddableWebComponents(false)
                .withConnectJavaSourceFolder(src)
                .withConnectGeneratedOpenApiJson(json)
                .withConnectClientTsApiFolder(new File(dir, "api"));

        builder.build().execute();

        Arrays.asList(
                // enableClientSide
                "target/index.html", "target/index.ts",
                // withConnectJavaSourceFolder and
                // withConnectGeneratedOpenApiJson
                "api-file.json",
                // withConnectClientTsApiFolder
                "api/connect-client.default.ts", "api/MyEndpoint.ts")
                .forEach(name -> assertTrue(name + " not created.",
                        new File(dir, name).exists()));
    }
}
