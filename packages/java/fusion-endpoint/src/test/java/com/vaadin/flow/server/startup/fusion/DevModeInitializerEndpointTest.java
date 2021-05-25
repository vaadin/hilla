package com.vaadin.flow.server.startup.fusion;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.di.ResourceProvider;
import com.vaadin.flow.server.DevModeHandler;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.fusion.EndpointGeneratorTaskFactoryImpl;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.server.startup.DevModeInitializer;

import static com.vaadin.flow.server.Constants.CONNECT_JAVA_SOURCE_FOLDER_TOKEN;
import static com.vaadin.flow.server.Constants.TARGET;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_CONNECT_JAVA_SOURCE_FOLDER;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_CONNECT_OPENAPI_JSON_FILE;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_FLOW_RESOURCES_FOLDER;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_GENERATED_DIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class DevModeInitializerEndpointTest {

    String baseDir;
    ServletContext servletContext;
    Set<Class<?>> classes;
    DevModeInitializer devModeInitializer;
    private ApplicationConfiguration appConfig;

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static class VaadinServletSubClass extends VaadinServlet {

    }

    @Before
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setup() throws Exception {
        assertNull("No DevModeHandler should be available at test start",
                DevModeHandler.getDevModeHandler());

        temporaryFolder.create();
        baseDir = temporaryFolder.getRoot().getPath();

        Files.write(new File(baseDir, "package.json").toPath(),
                "{}".getBytes(StandardCharsets.UTF_8));

        final File generatedDirectory = new File(baseDir,
                Paths.get(TARGET, DEFAULT_GENERATED_DIR).toString());
        FileUtils.forceMkdir(generatedDirectory);

        Files.write(new File(generatedDirectory, "package.json").toPath(),
                "{}".getBytes(StandardCharsets.UTF_8));

        appConfig = Mockito.mock(ApplicationConfiguration.class);
        Mockito.when(appConfig.getStringProperty(Mockito.anyString(),
                Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        Mockito.when(appConfig.getStringProperty(FrontendUtils.PROJECT_BASEDIR,
                null)).thenReturn(baseDir);
        Mockito.when(appConfig.enableDevServer()).thenReturn(true);
        Mockito.when(appConfig.isPnpmEnabled()).thenReturn(true);
        Mockito.when(appConfig.getBooleanProperty(
                Mockito.matches(SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE),
                Mockito.anyBoolean())).thenReturn(false);
        Mockito.when(appConfig.getBuildFolder()).thenReturn(TARGET);
        Mockito.when(appConfig.getFlowResourcesFolder())
                .thenReturn(TARGET + "/" + DEFAULT_FLOW_RESOURCES_FOLDER);

        servletContext = mockServletContext();
        ServletRegistration vaadinServletRegistration = Mockito
                .mock(ServletRegistration.class);

        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(servletContext.getAttribute(Lookup.class.getName()))
                .thenReturn(lookup);
        Mockito.doReturn(new EndpointGeneratorTaskFactoryImpl()).when(lookup)
                .lookup(EndpointGeneratorTaskFactory.class);

        ResourceProvider resourceProvider = Mockito
                .mock(ResourceProvider.class);
        Mockito.when(lookup.lookup(ResourceProvider.class))
                .thenReturn(resourceProvider);

        Mockito.when(vaadinServletRegistration.getClassName())
                .thenReturn(VaadinServletSubClass.class.getName());

        classes = new HashSet<>();
        classes.add(this.getClass());

        Map registry = new HashMap();

        // Adding extra registrations to make sure that DevModeInitializer picks
        // the correct registration which is a VaadinServlet registration.
        registry.put("extra1", Mockito.mock(ServletRegistration.class));
        registry.put("foo", vaadinServletRegistration);
        registry.put("extra2", Mockito.mock(ServletRegistration.class));
        Mockito.when(servletContext.getServletRegistrations())
                .thenReturn(registry);
        Mockito.when(servletContext.getInitParameterNames())
                .thenReturn(Collections.emptyEnumeration());
        Mockito.when(servletContext.getClassLoader())
                .thenReturn(this.getClass().getClassLoader());

        FileUtils.forceMkdir(
                new File(baseDir, DEFAULT_CONNECT_JAVA_SOURCE_FOLDER));

        devModeInitializer = new DevModeInitializer();
    }

    @After
    public void teardown() throws Exception {
        final DevModeHandler devModeHandler = DevModeHandler
                .getDevModeHandler();
        if (devModeHandler != null) {
            devModeHandler.stop();
            // Wait until dev mode handler has stopped.
            while (DevModeHandler.getDevModeHandler() != null) {
                Thread.sleep(200); // NOSONAR
            }
        }

        temporaryFolder.delete();
    }

    @Test
    public void should_generateOpenApi_when_EndpointPresents()
            throws Exception {
        File generatedOpenApiJson = Paths
                .get(baseDir, TARGET, DEFAULT_CONNECT_OPENAPI_JSON_FILE)
                .toFile();
        File src = new File(
                getClass().getClassLoader().getResource("java").getFile());
        Mockito.when(appConfig.getStringProperty(
                Mockito.eq(CONNECT_JAVA_SOURCE_FOLDER_TOKEN),
                Mockito.anyString())).thenReturn(src.getAbsolutePath());

        Assert.assertFalse(generatedOpenApiJson.exists());
        DevModeInitializer devModeInitializer = new DevModeInitializer();
        devModeInitializer.onStartup(classes, servletContext);
        waitForDevModeServer();
        Assert.assertTrue("Should generate OpenAPI spec if Endpoint is used.",
                generatedOpenApiJson.exists());
    }

    @Test
    public void should_notGenerateOpenApi_when_EndpointIsNotUsed()
            throws Exception {
        File generatedOpenApiJson = Paths
                .get(baseDir, TARGET, DEFAULT_CONNECT_OPENAPI_JSON_FILE)
                .toFile();

        Assert.assertFalse(generatedOpenApiJson.exists());
        devModeInitializer.onStartup(classes, servletContext);
        waitForDevModeServer();
        Assert.assertFalse(
                "Should not generate OpenAPI spec if Endpoint is not used.",
                generatedOpenApiJson.exists());
    }

    @Test
    public void should_generateTs_files() throws Exception {
        // Configure a folder that has .java classes with valid endpoints
        // Not using `src/test/java` because there are invalid endpoint
        // names
        // in some tests
        File src = new File(
                getClass().getClassLoader().getResource("java").getFile());
        Mockito.when(appConfig.getStringProperty(
                Mockito.eq(CONNECT_JAVA_SOURCE_FOLDER_TOKEN),
                Mockito.anyString())).thenReturn(src.getAbsolutePath());

        DevModeInitializer devModeInitializer = new DevModeInitializer();

        File ts1 = new File(baseDir,
                DEFAULT_PROJECT_FRONTEND_GENERATED_DIR + "MyEndpoint.ts");
        File ts2 = new File(baseDir, DEFAULT_PROJECT_FRONTEND_GENERATED_DIR
                + "connect-client.default.ts");

        assertFalse(ts1.exists());
        assertFalse(ts2.exists());
        devModeInitializer.onStartup(classes, servletContext);
        waitForDevModeServer();
        assertTrue(ts1.exists());
        assertTrue(ts2.exists());
    }

    private void waitForDevModeServer() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        DevModeHandler handler = DevModeHandler.getDevModeHandler();
        Assert.assertNotNull(handler);
        Method join = DevModeHandler.class.getDeclaredMethod("join");
        join.setAccessible(true);
        join.invoke(handler);
    }

    private ServletContext mockServletContext() {
        ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(
                context.getAttribute(ApplicationConfiguration.class.getName()))
                .thenReturn(appConfig);
        return context;
    }

}
