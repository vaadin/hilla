package dev.hilla;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class ServletContextTestSetup implements ServletContextAware {

    @MockBean
    private FeatureFlagCondition featureFlagCondition;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        checkStaticAppContext();
        Lookup lookup = Mockito.mock(Lookup.class);
        servletContext.setAttribute(Lookup.class.getName(), lookup);
        ApplicationConfiguration applicationConfiguration = Mockito
                .mock(ApplicationConfiguration.class);
        servletContext.setAttribute(ApplicationConfiguration.class.getName(),
                applicationConfiguration);
        Mockito.when(featureFlagCondition.matches(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        try {
            Path tmpDir = Files.createTempDirectory("test");
            Path target = tmpDir.resolve("target");
            target.toFile().mkdir();
            Mockito.when(applicationConfiguration.getProjectFolder())
                    .thenReturn(tmpDir.toFile());

            Mockito.when(applicationConfiguration.getBuildFolder())
                    .thenReturn(target.getFileName().toString());
            Mockito.when(applicationConfiguration
                    .getStringProperty(Mockito.anyString(), Mockito.any()))
                    .thenAnswer(q -> {
                        return q.getArgument(1); // The default value
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkStaticAppContext() {
        try {
            Field f = ApplicationContextProvider.class
                    .getDeclaredField("applicationContext");
            f.setAccessible(true);
            ApplicationContext appContext = (ApplicationContext) f.get(null);
            if (appContext != null && appContext != applicationContext) {
                getLogger().error("Application context is " + applicationContext
                        + " for the test but the static field in "
                        + ApplicationContextProvider.class.getName() + " is "
                        + appContext);
                f.set(null, applicationContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
