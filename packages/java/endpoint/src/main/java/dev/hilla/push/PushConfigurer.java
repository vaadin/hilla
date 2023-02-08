package dev.hilla.push;

import java.util.Collections;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.ContainerInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import dev.hilla.ConditionalOnFeatureFlag;
import dev.hilla.EndpointInvoker;

/**
 * Defines the beans needed for push in Hilla.
 */
@Configuration
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
public class PushConfigurer {

    @Bean
    public PushMessageHandler pushMessageHandler(
            EndpointInvoker endpointInvoker) {
        return new PushMessageHandler(endpointInvoker);
    }

    @Bean
    public EmbeddedAtmosphereInitializer atmosphereInitializer() {
        return new EmbeddedAtmosphereInitializer();
    }

    @Bean
    public ServletRegistrationBean<AtmosphereServlet> atmosphereServlet() {
        ServletRegistrationBean<AtmosphereServlet> registration = new ServletRegistrationBean<>(
                new AtmosphereServlet(), "/HILLA/push");

        // Override the global mapping set by Flow
        registration.addInitParameter(ApplicationConfig.JSR356_MAPPING_PATH,
                "/HILLA/push");
        registration.setLoadOnStartup(0);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    static class EmbeddedAtmosphereInitializer
            extends ContainerInitializer implements ServletContextInitializer {

        @Override
        public void onStartup(ServletContext servletContext)
                throws ServletException {
            onStartup(Collections.<Class<?>> emptySet(), servletContext);
        }

    }

}
