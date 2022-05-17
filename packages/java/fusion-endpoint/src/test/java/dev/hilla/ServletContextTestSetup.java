package dev.hilla;

import javax.servlet.ServletContext;

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
    }

}
