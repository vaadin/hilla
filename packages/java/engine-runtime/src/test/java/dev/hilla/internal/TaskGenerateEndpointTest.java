package dev.hilla.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskGenerateEndpointTest extends TaskTest {

    private TaskGenerateEndpoint taskGenerateEndpoint;
    private Path outputDirectory;
    private File openApiJson;

    @BeforeEach
    public void setUp() throws IOException {
        outputDirectory = Files
                .createDirectory(getTemporaryDirectory().resolve("output"));
        openApiJson = new File(getClass().getResource(
                "openapi/esmodule-generator-TwoEndpointsThreeMethods.json")
                .getPath());
    }

    @Test
    public void should_generate_Two_TypeScriptFiles() throws Exception {
        File ts1 = outputDirectory.resolve("FooBarEndpoint.ts").toFile();
        File ts2 = outputDirectory.resolve("FooFooEndpoint.ts").toFile();
        File client = outputDirectory.resolve("connect-client.default.ts")
                .toFile();

        assertFalse(ts1.exists());
        assertFalse(ts2.exists());
        assertFalse(client.exists());

        taskGenerateEndpoint = new TaskGenerateEndpointImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                openApiJson, outputDirectory.toFile());
        taskGenerateEndpoint.execute();

        assertTrue(ts1.exists());
        assertTrue(ts2.exists());
        assertTrue(client.exists());

        String output = FileUtils.readFileToString(client, "UTF-8");
        assertThat(output, containsString(
                "import { ConnectClient as ConnectClient_1 } from \"@hilla/frontend\";"));
        assertThat(output, containsString(
                "const client_1 = new ConnectClient_1({ prefix: \"connect\" });"));
        assertThat(output, containsString("export default client_1;"));
    }

    @Test
    public void should_use_custom_endpoint_name_when_connect_client_exists()
            throws Exception {
        File ts1 = outputDirectory.resolve("FooBarEndpoint.ts").toFile();
        File ts2 = outputDirectory.resolve("FooFooEndpoint.ts").toFile();
        File client = outputDirectory.resolve("connect-client.default.ts")
                .toFile();
        File customConnectClient = Files
                .createFile(
                        getTemporaryDirectory().resolve("connect-client.ts"))
                .toFile();

        assertFalse(ts1.exists());
        assertFalse(ts2.exists());
        assertFalse(client.exists());
        assertTrue(customConnectClient.exists());

        taskGenerateEndpoint = new TaskGenerateEndpointImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                openApiJson, outputDirectory.toFile());
        taskGenerateEndpoint.execute();

        assertTrue(ts1.exists());
        assertTrue(ts2.exists());
        assertFalse(client.exists());

        String outputEndpoinTs1 = FileUtils.readFileToString(ts1, "UTF-8");
        String outputEndpoinTs2 = FileUtils.readFileToString(ts2, "UTF-8");
        assertThat(outputEndpoinTs1,
                containsString("import client_1 from \"../connect-client\";"));
        assertThat(outputEndpoinTs2,
                containsString("import client_1 from \"../connect-client\";"));
    }
}
