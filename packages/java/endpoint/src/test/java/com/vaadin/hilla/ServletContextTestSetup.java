package com.vaadin.hilla;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class ServletContextTestSetup implements ServletContextAware {

    @MockBean
    private FeatureFlagCondition featureFlagCondition;

    @Override
    public void setServletContext(ServletContext servletContext) {
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

}
