package dev.hilla.push;

import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.dependency.NpmPackage;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.hilla.ConditionalOnFeatureFlag;
import dev.hilla.push.messages.fromclient.AbstractServerMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;

/**
 * Sets up and configures socket.io to handle messages from the client side.
 */
@Service
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
public class SocketIoHandler {

    /**
     * Creates the handler.
     *
     * @param socketIoServer
     *            the socket io server
     * @param objectMapper
     *            the object mapper to use for JSON serialization
     * @param pushMessageHandler
     *            the handler for incoming messages
     */
    public SocketIoHandler(SocketIoServer socketIoServer,
            ObjectMapper objectMapper, PushMessageHandler pushMessageHandler) {

        SocketIoNamespace hillaNamespace = socketIoServer.namespace("hilla");
        hillaNamespace.on("connection", event -> {
            SocketIoSocket socket = (SocketIoSocket) event[0];

            Consumer<AbstractClientMessage> sender = message -> {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Sending push message to the client: "
                                + message);
                    }
                    socket.send("message",
                            objectMapper.writeValueAsString(message));
                } catch (JsonProcessingException
                        | IllegalArgumentException e1) {
                    getLogger().warn(
                            "Unexpected problem when sending push message", e1);
                }
            };

            socket.on("message", messageEvent -> {
                JSONObject json = (JSONObject) messageEvent[0];
                try {
                    AbstractServerMessage message = objectMapper.readValue(
                            json.toString(), AbstractServerMessage.class);
                    if (getLogger().isDebugEnabled()) {
                        getLogger()
                                .debug("Received push message from the client: "
                                        + message);
                    }
                    pushMessageHandler.handleMessage(message, sender);
                } catch (JsonProcessingException e1) {
                    getLogger().warn(
                            "Unexpected problem when receiving push message",
                            e1);
                }
            });
        });
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}
