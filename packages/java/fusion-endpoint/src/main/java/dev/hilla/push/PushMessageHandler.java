package dev.hilla.push;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.server.VaadinServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.hilla.AuthenticationUtil;
import dev.hilla.ConditionalOnFeatureFlag;
import dev.hilla.EndpointInvocationException.EndpointAccessDeniedException;
import dev.hilla.EndpointInvocationException.EndpointBadRequestException;
import dev.hilla.EndpointInvocationException.EndpointInternalException;
import dev.hilla.EndpointInvocationException.EndpointNotFoundException;
import dev.hilla.EndpointInvoker;
import dev.hilla.push.messages.fromclient.AbstractServerMessage;
import dev.hilla.push.messages.fromclient.SubscribeMessage;
import dev.hilla.push.messages.fromclient.UnsubscribeMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;
import dev.hilla.push.messages.toclient.ClientMessageComplete;
import dev.hilla.push.messages.toclient.ClientMessageError;
import dev.hilla.push.messages.toclient.ClientMessageUpdate;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * Handles incoming requests from the client and connects them to fluxes
 * returned from endpoints.
 */
@Service
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
public class PushMessageHandler {

    static final String PUSH_FEATURE_FLAG = "hillaPush";

    private final EndpointInvoker endpointInvoker;

    /*
     * Maps from connection id to subscription id inside that connection to the
     * actual objects so that we can clean up everything related to a connection
     * id on disconnect
     */
    ConcurrentHashMap<String, ConcurrentHashMap<String, Disposable>> fluxSubscriptionDisposables = new ConcurrentHashMap<>();

    @Autowired
    private ServletContext servletContext;

    /**
     * Creates the instance.
     *
     * @param endpointInvoker
     *            the endpoint invoker
     */
    public PushMessageHandler(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    /**
     * Handles the message.
     *
     * @param connectionId
     *            an id uniquely identifying the underlying (shared) connection
     * @param message
     *            the message from the client
     * @param sender
     *            a method that sends a message back to the client
     */
    public void handleMessage(String connectionId,
            AbstractServerMessage message,
            Consumer<AbstractClientMessage> sender) {
        if (message instanceof SubscribeMessage) {
            handleBrowserSubscribe(connectionId, (SubscribeMessage) message,
                    sender);
        } else if (message instanceof UnsubscribeMessage) {
            handleBrowserUnsubscribe(connectionId,
                    (UnsubscribeMessage) message);
        } else {
            throw new IllegalArgumentException(
                    "Unknown message type: " + message.getClass().getName());
        }
    }

    private void handleBrowserSubscribe(String connectionId,
            SubscribeMessage message, Consumer<AbstractClientMessage> sender) {
        String fluxId = message.getId();

        FeatureFlags featureFlags = FeatureFlags
                .get(new VaadinServletContext(servletContext));
        if (!featureFlags.isEnabled(FeatureFlags.HILLA_PUSH)) {
            String msg = featureFlags
                    .getEnableHelperMessage(FeatureFlags.HILLA_PUSH);
            getLogger().error(msg);
            sender.accept(new ClientMessageError(fluxId, msg));
            return;

        }
        if (endpointInvoker.getReturnType(message.getEndpointName(),
                message.getMethodName()) != Flux.class) {
            sender.accept(new ClientMessageError(fluxId,
                    "Method " + message.getEndpointName() + "/"
                            + message.getMethodName()
                            + " is not a Flux method"));
            return;
        }

        ArrayNode paramsArray = message.getParams();
        ObjectNode paramsObject = paramsArray.objectNode();
        for (int i = 0; i < paramsArray.size(); i++) {
            paramsObject.set(i + "", paramsArray.get(i));
        }

        Principal principal = AuthenticationUtil
                .getSecurityHolderAuthentication();
        Function<String, Boolean> isInRole = AuthenticationUtil
                .getSecurityHolderRoleChecker();

        try {
            Flux<?> flux = (Flux<?>) endpointInvoker.invoke(
                    message.getEndpointName(), message.getMethodName(),
                    paramsObject, principal, isInRole);
            Disposable endpointFluxSubscriber = flux.subscribe(item -> {
                send(sender, new ClientMessageUpdate(fluxId, item));
            }, error -> {
                // An exception was thrown from the Flux
                disposeSubscriptionInfo(connectionId, fluxId);
                send(sender,
                        new ClientMessageError(fluxId, "Exception in Flux"));
                getLogger().error("Exception in Flux", error);
            }, () -> {
                // Flux completed
                disposeSubscriptionInfo(connectionId, fluxId);
                send(sender, new ClientMessageComplete(fluxId));
            });
            fluxSubscriptionDisposables.get(connectionId).put(fluxId,
                    endpointFluxSubscriber);

        } catch (EndpointNotFoundException e) {
            sender.accept(new ClientMessageError(fluxId, "No such endpoint"));
            return;
        } catch (EndpointAccessDeniedException | EndpointBadRequestException
                | EndpointInternalException e) {
            sender.accept(new ClientMessageError(fluxId, e.getMessage()));
            return;
        }

    }

    private void send(Consumer<AbstractClientMessage> sender,
            AbstractClientMessage message) {
        sender.accept(message);

    }

    /**
     * Called when the browser establishes a new connection.
     *
     * Only ever called once for the same connectionId parameter.
     *
     * @param connectionId
     *            the id of the connection
     */
    public void handleBrowserConnect(String connectionId) {
        fluxSubscriptionDisposables.put(connectionId,
                new ConcurrentHashMap<>());
    }

    /**
     * Called when the browser connection has been lost.
     *
     * Only ever called once for the same connectionId parameter. The same
     * connectionId parameter will never be used after this call.
     *
     * @param connectionId
     *            the id of the connection
     */
    public void handleBrowserDisconnect(String connectionId) {
        disposeConnectionInfo(connectionId);
    }

    private void handleBrowserUnsubscribe(String connectionId,
            UnsubscribeMessage message) {
        String fluxId = message.getId();
        disposeSubscriptionInfo(connectionId, fluxId);
    }

    /**
     * Removes all stored data related to the given connection. Disposes any
     * active subscriptions.
     *
     * @param connectionId
     *            the connection id
     */
    private void disposeConnectionInfo(String connectionId) {
        ConcurrentHashMap<String, Disposable> fluxMap = fluxSubscriptionDisposables
                .remove(connectionId);
        if (fluxMap != null) {
            fluxMap.forEach((cid, fluxSubscriptionDisposable) -> {
                dispose(fluxSubscriptionDisposable);
            });
        }
    }

    /**
     * Removes all stored data related to the given subscription in the given
     * connection.
     *
     * @param connectionId
     *            the connection id
     * @param subscriptionId
     *            the subscription id
     */
    private void disposeSubscriptionInfo(String connectionId,
            String subscriptionId) {
        ConcurrentHashMap<String, Disposable> fluxMap = fluxSubscriptionDisposables
                .get(connectionId);
        if (fluxMap != null) {
            Disposable subscriptionInfo = fluxMap.remove(subscriptionId);
            if (subscriptionInfo != null) {
                dispose(subscriptionInfo);
            }
        }
    }

    private void dispose(Disposable fluxSubscriptionDisposable) {
        fluxSubscriptionDisposable.dispose();
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
