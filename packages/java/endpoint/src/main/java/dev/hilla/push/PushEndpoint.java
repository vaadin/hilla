package dev.hilla.push;

import java.io.IOException;
import java.security.Principal;
import java.util.function.Consumer;

import javax.servlet.ServletContext;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.config.service.Singleton;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.util.SimpleBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.push.messages.fromclient.AbstractServerMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Sets up and configures the push channel.
 * <p>
 * This class is initialized by Atmosphere and not Spring, so autowiring is
 * handled manually when the first request comes in.
 */
@AtmosphereHandlerService(path = "/HILLA/push", broadcaster = SimpleBroadcaster.class, interceptors = {
        AtmosphereResourceLifecycleInterceptor.class,
        TrackMessageSizeInterceptor.class,
        BroadcastOnPostAtmosphereInterceptor.class,
        SuspendTrackerInterceptor.class })
@Singleton
public class PushEndpoint extends AtmosphereHandlerAdapter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PushMessageHandler pushMessageHandler;

    public PushEndpoint() {
        /*
         * Atmosphere requires there to be a no-arg constructor unless a custom
         * AtmosphereObjectFactory is used
         */
    }

    private void autowire(ServletContext servletContext) {
        WebApplicationContext ac = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        ac.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        if (objectMapper == null) {
            autowire(resource.getAtmosphereConfig().getServletContext());
        }
        if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
            onConnect(resource);
        }
        super.onRequest(resource);
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event)
            throws IOException {
        super.onStateChange(event);
        if (event.isCancelled() || event.isResumedOnTimeout()) {
            onDisconnect(event);
        } else if (event.isSuspended()) {
            String message = event.getMessage() == null ? null : event.getMessage().toString();
            if (message != null) {
                Principal p = event.getResource().getRequest().getUserPrincipal();
                SecurityContextHolder
                        .setContext(new SecurityContextImpl((Authentication) p));
                try {
                    onMessage(event, message);
                } finally {
                    SecurityContextHolder.clearContext();
                }

            } else {
                getLogger().error("Received null message in push channel");
            }
        }
    }

    /**
     * Called when the client sends a message through the push channel.
     *
     * @param event
     *                          the Atmosphere event
     * @param messageFromClient the received message
     */
    private void onMessage(AtmosphereResourceEvent event, String messageFromClient) {
        try {
            AbstractServerMessage message = objectMapper.readValue(
                    messageFromClient, AbstractServerMessage.class);
            if (getLogger().isDebugEnabled()) {
                getLogger()
                        .debug("Received push message from the client: "
                                + message);
            }
            Consumer<AbstractClientMessage> sender = msg -> {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Sending push message to the client: "
                                + msg);
                    }
                    event.getResource().write(
                            objectMapper.writeValueAsString(msg));
                } catch (JsonProcessingException
                        | IllegalArgumentException e1) {
                    getLogger().warn(
                            "Unexpected problem when sending push message", e1);
                }
            };

            pushMessageHandler.handleMessage(event.getResource().uuid(), message,
                    sender);
        } catch (JsonProcessingException e) {
            getLogger().warn(
                    "Unexpected problem when receiving push message",
                    e);
        }

    }

    /**
     * Called when the client sends the first request (to establish a push
     * connection).
     *
     * @param resource
     *                 the resource which was connected
     */
    private void onConnect(AtmosphereResource resource) {
        pushMessageHandler.handleBrowserConnect(resource.uuid());
        resource.addEventListener(new DisconnectListener(this));
    }

    private static class DisconnectListener extends AtmosphereResourceEventListenerAdapter {

        private PushEndpoint pushEndpoint;

        public DisconnectListener(PushEndpoint pushEndpoint) {
            this.pushEndpoint = pushEndpoint;
        }

        @Override
        public void onDisconnect(AtmosphereResourceEvent event) {
            super.onDisconnect(event);
            pushEndpoint.onDisconnect(event);
        }

        @Override
        public void onThrowable(AtmosphereResourceEvent event) {
            super.onThrowable(event);
            pushEndpoint.onThrowable(event);
        }
    }

    /**
     * Called when the push channel is disconnected.
     * 
     * @param event the Atmosphere event
     */
    private void onDisconnect(AtmosphereResourceEvent event) {
        pushMessageHandler.handleBrowserDisconnect(event.getResource().uuid());
    }

    private void onThrowable(AtmosphereResourceEvent event) {
        getLogger().error("Exception in push connection",
                event.throwable());
        onDisconnect(event);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
