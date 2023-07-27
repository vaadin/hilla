package dev.hilla.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.util.SimpleBroadcaster;
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

    private static final String HILLA_PUSH_PATH = "/HILLA/push";

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
        AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
        ServletRegistrationBean<AtmosphereServlet> registration = new ServletRegistrationBean<>(
                atmosphereServlet, HILLA_PUSH_PATH);

        List<AtmosphereInterceptor> interceptors = Arrays.asList(
                new AtmosphereResourceLifecycleInterceptor(),
                new TrackMessageSizeInterceptor(),
                new SuspendTrackerInterceptor());
        AtmosphereFramework fw = atmosphereServlet.framework();
        fw.addAtmosphereHandler(HILLA_PUSH_PATH, pushEndpoint, interceptors);

        // Override the global mapping set by Flow
        registration.addInitParameter(ApplicationConfig.JSR356_MAPPING_PATH,
                HILLA_PUSH_PATH);
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

}
