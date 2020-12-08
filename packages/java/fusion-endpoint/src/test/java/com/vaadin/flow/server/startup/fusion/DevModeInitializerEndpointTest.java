package com.vaadin.flow.server.startup.fusion;

import static com.vaadin.flow.server.Constants.CONNECT_JAVA_SOURCE_FOLDER_TOKEN;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_PRODUCTION_MODE;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_REUSE_DEV_SERVER;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_CONNECT_GENERATED_TS_DIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_CONNECT_JAVA_SOURCE_FOLDER;
import static com.vaadin.flow.server.frontend.FrontendUtils.DEFAULT_CONNECT_OPENAPI_JSON_FILE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.di.ResourceProvider;
import com.vaadin.flow.server.DevModeHandler;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.fusion.EndpointGeneratorTaskFactoryImpl;
import com.vaadin.flow.server.startup.DevModeInitializer;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DevModeInitializerEndpointTest {
    private final AtomicReference<DevModeHandler> atomicHandler = new AtomicReference<>();
    
    String baseDir;
    ServletContext servletContext;
    Map<String, String> initParams;
    Set<Class<?>> classes;
    DevModeInitializer devModeInitializer;

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static class VaadinServletSubClass extends VaadinServlet {

    }

    @Before
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setup() throws Exception {
        assertNull(getDevModeHandler());

        temporaryFolder.create();
        baseDir = temporaryFolder.getRoot().getPath();

        servletContext = Mockito.mock(ServletContext.class);
        ServletRegistration vaadinServletRegistration = Mockito
                .mock(ServletRegistration.class);

        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(servletContext.getAttribute(Lookup.class.getName()))
                .thenReturn(lookup);
        Mockito.doReturn(new EndpointGeneratorTaskFactoryImpl()).when(lookup).lookup(EndpointGeneratorTaskFactory.class);

        ResourceProvider resourceProvider = Mockito
                .mock(ResourceProvider.class);
        Mockito.when(lookup.lookup(ResourceProvider.class))
                .thenReturn(resourceProvider);

        Mockito.when(vaadinServletRegistration.getClassName())
                .thenReturn(VaadinServletSubClass.class.getName());

        initParams = new HashMap<>();
        initParams.put(FrontendUtils.PROJECT_BASEDIR, baseDir);

        Mockito.when(vaadinServletRegistration.getInitParameters())
                .thenReturn(initParams);

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
    public void teardown() throws Exception, SecurityException {
        System.clearProperty("vaadin." + SERVLET_PARAMETER_PRODUCTION_MODE);
        System.clearProperty("vaadin." + SERVLET_PARAMETER_REUSE_DEV_SERVER);
        System.clearProperty("vaadin." + CONNECT_JAVA_SOURCE_FOLDER_TOKEN);

        temporaryFolder.delete();
        if (getDevModeHandler() != null) {
            getDevModeHandler().stop();
        }
    }

    @Test
    public void should_generateOpenApi_when_EndpointPresents()
            throws Exception {

        // Configure a folder that has .java classes with valid endpoints
        // Not using `src/test/java` because there are invalid endpoint names
        // in some tests
        File src = new File(
                getClass().getClassLoader().getResource("java").getFile());
        System.setProperty("vaadin." + CONNECT_JAVA_SOURCE_FOLDER_TOKEN,
                src.getAbsolutePath());

        File generatedOpenApiJson = Paths
                .get(baseDir, DEFAULT_CONNECT_OPENAPI_JSON_FILE).toFile();

        Assert.assertFalse(generatedOpenApiJson.exists());
        DevModeInitializer devModeInitializer = new DevModeInitializer();
        devModeInitializer.onStartup(classes, servletContext);
        waitForDevModeServer();
        Thread.sleep(200);
        Assert.assertTrue("Should generate OpenAPI spec if Endpoint is used.",
                generatedOpenApiJson.exists());
    }

    @Test
    public void should_notGenerateOpenApi_when_EndpointIsNotUsed()
            throws Exception {
        File generatedOpenApiJson = Paths
                .get(baseDir, DEFAULT_CONNECT_OPENAPI_JSON_FILE).toFile();
        Assert.assertFalse(generatedOpenApiJson.exists());
        devModeInitializer.onStartup(classes, servletContext);
        Assert.assertFalse(
                "Should not generate OpenAPI spec if Endpoint is not used.",
                generatedOpenApiJson.exists());
    }

    @Test
    public void should_generateTs_files() throws Exception {

        // Configure a folder that has .java classes with valid endpoints
        // Not using `src/test/java` because there are invalid endpoint names
        // in some tests
        File src = new File(
                getClass().getClassLoader().getResource("java").getFile());
        System.setProperty("vaadin." + CONNECT_JAVA_SOURCE_FOLDER_TOKEN,
                src.getAbsolutePath());

        DevModeInitializer devModeInitializer = new DevModeInitializer();

        File ts1 = new File(baseDir,
                DEFAULT_CONNECT_GENERATED_TS_DIR + "MyEndpoint.ts");
        File ts2 = new File(baseDir, DEFAULT_CONNECT_GENERATED_TS_DIR
                + "connect-client.default.ts");

        assertFalse(ts1.exists());
        assertFalse(ts2.exists());
        devModeInitializer.onStartup(classes, servletContext);
        waitForDevModeServer();
        assertTrue(ts1.exists());
        assertTrue(ts2.exists());
    }

    /**
     * Get the instantiated DevModeHandler.
     *
     * @return devModeHandler or {@code null} if not started
     */
    private DevModeHandler getDevModeHandler() {
      return atomicHandler.get();
    }

    private void waitForDevModeServer() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InterruptedException {
        DevModeHandler handler = DevModeHandler.getDevModeHandler();
        Assert.assertNotNull(handler);
        Method join = DevModeHandler.class.getDeclaredMethod("join");
        join.setAccessible(true);
        join.invoke(handler);
    }

}
