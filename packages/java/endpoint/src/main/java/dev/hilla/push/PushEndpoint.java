package dev.hilla.push;

import java.io.IOException;
import java.security.Principal;
import java.util.function.Consumer;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.push.messages.fromclient.AbstractServerMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Sets up and configures the push channel.
 */
public class PushEndpoint extends AtmosphereHandlerAdapter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PushMessageHandler pushMessageHandler;

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        String method = resource.getRequest().getMethod();
        if (method.equalsIgnoreCase("GET")) {
            onConnect(resource);
        } else if (method.equalsIgnoreCase("POST")) {
            onMessageRequest(resource);
        }
        super.onRequest(resource);
    }

    private void onMessageRequest(AtmosphereResource resource) {
        // This is copied from BroadcastOnPostAtmosphereInterceptor but does not
        // use the broadcaster as the message should only go to this one channel
        // and not
        // all push channels
        AtmosphereRequest request = resource.getRequest();
        try {
            Object o = IOUtils.readEntirely(resource);
            if (IOUtils.isBodyEmpty(o)) {
                getLogger().warn("Received an empty body for push message {}",
                        request);
                return;
            }

            String message = o == null ? null : o.toString();
            if (message != null) {
                Principal p = request.getUserPrincipal();
                SecurityContextHolder.setContext(
                        new SecurityContextImpl((Authentication) p));
                try {
                    onMessage(resource, message);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (IOException e) {
            getLogger().warn("Unable to read push message {}", request, e);
            return;
        }

    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event)
            throws IOException {
        super.onStateChange(event);
        if (event.isCancelled() || event.isResumedOnTimeout()) {
            onDisconnect(event);
        }
    }

    /**
     * Called when the client sends a message through the push channel.
     *
     * @param event
     *            the Atmosphere resource that received the message
     * @param messageFromClient
     *            the received message
     */
    private void onMessage(AtmosphereResource resource,
            String messageFromClient) {
        try {
            AbstractServerMessage message = objectMapper
                    .readValue(messageFromClient, AbstractServerMessage.class);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                        "Received push message from the client: " + message);
            }
            Consumer<AbstractClientMessage> sender = msg -> {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(
                                "Sending push message to the client: " + msg);
                    }
                    resource.write(objectMapper.writeValueAsString(msg));
                } catch (JsonProcessingException
                        | IllegalArgumentException e1) {
                    getLogger().warn(
                            "Unexpected problem when sending push message", e1);
                }
            };

            pushMessageHandler.handleMessage(resource.uuid(), message, sender);
        } catch (JsonProcessingException e) {
            getLogger().warn("Unexpected problem when receiving push message",
                    e);
        }

    }

    /**
     * Called when the client sends the first request (to establish a push
     * connection).
     *
     * @param resource
     *            the resource which was connected
     */
    private void onConnect(AtmosphereResource resource) {
        pushMessageHandler.handleBrowserConnect(resource.uuid());
        resource.addEventListener(new DisconnectListener(this));
    }

    private static class DisconnectListener
            extends AtmosphereResourceEventListenerAdapter {

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
     * @param event
     *            the Atmosphere event
     */
    private void onDisconnect(AtmosphereResourceEvent event) {
        pushMessageHandler.handleBrowserDisconnect(event.getResource().uuid());
    }

    private void onThrowable(AtmosphereResourceEvent event) {
        getLogger().error("Exception in push connection", event.throwable());
        onDisconnect(event);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
