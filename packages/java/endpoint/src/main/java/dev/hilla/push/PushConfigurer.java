package dev.hilla.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import dev.hilla.ConditionalOnFeatureFlag;
import dev.hilla.EndpointInvoker;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Defines the beans needed for push in Hilla.
 */
@Configuration
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
public class PushConfigurer {

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
                atmosphereServlet, "/HILLA/push");

        List<AtmosphereInterceptor> interceptors = Arrays.asList(
                new AtmosphereResourceLifecycleInterceptor(),
                new TrackMessageSizeInterceptor(),
                new SuspendTrackerInterceptor());
        AtmosphereFramework fw = atmosphereServlet.framework();
        fw.setDefaultBroadcasterClassName(SimpleBroadcaster.class.getName());
        fw.addAtmosphereHandler("/HILLA/push", pushEndpoint, interceptors);

        // Override the global mapping set by Flow
        registration.addInitParameter(ApplicationConfig.JSR356_MAPPING_PATH,
                "/HILLA/push");
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
