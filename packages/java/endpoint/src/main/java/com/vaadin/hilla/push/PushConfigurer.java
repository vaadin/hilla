package com.vaadin.hilla.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vaadin.hilla.EndpointProperties;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.ContainerInitializer;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.util.SimpleBroadcaster;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.vaadin.hilla.ConditionalOnFeatureFlag;
import com.vaadin.hilla.EndpointInvoker;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Defines the beans needed for push in Hilla.
 */
@Configuration
public class PushConfigurer {

    private static final String HILLA_PUSH_PATH = "/HILLA/push";

    private final EndpointProperties endpointProperties;

    /**
     * Initializes the configuration for reactive endpoints.
     *
     * @param endpointProperties
     *            Hilla endpoint properties
     */
    public PushConfigurer(EndpointProperties endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    @Bean
    PushEndpoint pushEndpoint() {
        return new PushEndpoint();
    }

    @Bean
    PushMessageHandler pushMessageHandler(EndpointInvoker endpointInvoker) {
        return new PushMessageHandler(endpointInvoker);
    }

    @Bean
    EmbeddedAtmosphereInitializer atmosphereInitializer() {
        return new EmbeddedAtmosphereInitializer();
    }

    @Bean
    ServletRegistrationBean<AtmosphereServlet> atmosphereServlet(
            PushEndpoint pushEndpoint) {
        final String hillaPushPath = getHillaPushPath();
        AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
        ServletRegistrationBean<AtmosphereServlet> registration = new ServletRegistrationBean<>(
                atmosphereServlet, hillaPushPath);

        List<AtmosphereInterceptor> interceptors = Arrays.asList(
                new AtmosphereResourceLifecycleInterceptor(),
                new TrackMessageSizeInterceptor(),
                new SuspendTrackerInterceptor());
        AtmosphereFramework fw = atmosphereServlet.framework();
        fw.setDefaultBroadcasterClassName(SimpleBroadcaster.class.getName());
        fw.addAtmosphereHandler(hillaPushPath, pushEndpoint, interceptors);

        // Override the global mapping set by Flow
        registration.addInitParameter(ApplicationConfig.JSR356_MAPPING_PATH,
                hillaPushPath);
        registration.setLoadOnStartup(0);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    static class EmbeddedAtmosphereInitializer extends ContainerInitializer
            implements ServletContextInitializer {

        @Override
        public void onStartup(ServletContext servletContext)
                throws ServletException {
            onStartup(Collections.<Class<?>> emptySet(), servletContext);
        }

    }

    /**
     * Prepends the endpoint prefix URL from endpoint properties (ignoring the
     * "/connect" suffix, which is only applied to regular endpoints).
     *
     * @return path with prefix prepended
     */
    private String getHillaPushPath() {
        var prefix = endpointProperties.getEndpointPrefix()
                .replaceFirst("(^|\\/)connect$", "");
        prefix = prefix.startsWith("/") ? prefix.substring(1) : prefix;
        return prefix.isEmpty() ? HILLA_PUSH_PATH
                : "/" + prefix + HILLA_PUSH_PATH;
    }

}
