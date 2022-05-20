package dev.hilla.push;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import dev.hilla.ConditionalOnFeatureFlag;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import reactor.core.scheduler.Schedulers;

/**
 * Sets up handles for both http and websocket for engine.io, the low level
 * transport mechansim for socket.io.
 */
@Controller
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
public final class EngineIoHandler
        implements HandshakeInterceptor, WebSocketHandler {

    private static final String ATTRIBUTE_ENGINEIO_BRIDGE = "engineIo.bridge";
    private static final String ATTRIBUTE_ENGINEIO_QUERY = "engineIo.query";
    private static final String ATTRIBUTE_ENGINEIO_HEADERS = "engineIo.headers";

    private final EngineIoServer mEngineIoServer;

    /**
     * Creates the handler.
     *
     * @param engineIoServer
     *            the engine.io server to delegate to
     */
    public EngineIoHandler(EngineIoServer engineIoServer) {
        mEngineIoServer = engineIoServer;
    }

    @RequestMapping(value = "/HILLA/push", method = { RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.OPTIONS }, headers = "Connection!=Upgrade")
    public void httpHandler(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        mEngineIoServer.handleRequest(request, response);
    }

    /* HandshakeInterceptor */

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_ENGINEIO_QUERY, request.getURI().getQuery());
        attributes.put(ATTRIBUTE_ENGINEIO_HEADERS, request.getHeaders());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
    }

    /* WebSocketHandler */

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        final EngineIoSpringWebSocket webSocket = new EngineIoSpringWebSocket(
                webSocketSession);
        webSocketSession.getAttributes().put(ATTRIBUTE_ENGINEIO_BRIDGE,
                webSocket);
        mEngineIoServer.handleWebSocket(webSocket);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession,
            CloseStatus closeStatus) {
        ((EngineIoSpringWebSocket) webSocketSession.getAttributes()
                .get(ATTRIBUTE_ENGINEIO_BRIDGE))
                        .afterConnectionClosed(closeStatus);
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession,
            WebSocketMessage<?> webSocketMessage) {
        Principal p = webSocketSession.getPrincipal();
        SecurityContextHolder
                .setContext(new SecurityContextImpl((Authentication) p));
        try {
            ((EngineIoSpringWebSocket) webSocketSession.getAttributes()
                    .get(ATTRIBUTE_ENGINEIO_BRIDGE))
                            .handleMessage(webSocketMessage);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession,
            Throwable throwable) {
        ((EngineIoSpringWebSocket) webSocketSession.getAttributes()
                .get(ATTRIBUTE_ENGINEIO_BRIDGE))
                        .handleTransportError(throwable);
    }

    private static final class EngineIoSpringWebSocket
            extends EngineIoWebSocket {

        private final WebSocketSession mSession;
        private final Map<String, String> mQuery;
        private final Map<String, List<String>> mHeaders;

        EngineIoSpringWebSocket(WebSocketSession session) {
            mSession = session;

            final String queryString = (String) mSession.getAttributes()
                    .get(ATTRIBUTE_ENGINEIO_QUERY);
            if (queryString != null) {
                mQuery = ParseQS.decode(queryString);
            } else {
                mQuery = new HashMap<>();
            }
            this.mHeaders = (Map<String, List<String>>) mSession.getAttributes()
                    .get(ATTRIBUTE_ENGINEIO_HEADERS);
        }

        /* EngineIoWebSocket */

        @Override
        public Map<String, String> getQuery() {
            return mQuery;
        }

        @Override
        public void write(String message) throws IOException {
            mSession.sendMessage(new TextMessage(message));
        }

        @Override
        public void write(byte[] message) throws IOException {
            mSession.sendMessage(new BinaryMessage(message));
        }

        @Override
        public void close() {
            try {
                mSession.close();
            } catch (IOException ignore) { // NOSONAR
                // Ignore this as done in the engine.io example
            }
        }

        /* WebSocketHandler */

        void afterConnectionClosed(CloseStatus closeStatus) {
            emit("close");
        }

        void handleMessage(WebSocketMessage<?> message) {
            if (message.getPayload() instanceof String
                    || message.getPayload() instanceof byte[]) {
                emit("message", (Object) message.getPayload());
            } else {
                throw new RuntimeException(String.format(
                        "Invalid message type received: %s. Expected String or byte[].",
                        message.getPayload().getClass().getName()));
            }
        }

        void handleTransportError(Throwable exception) {
            emit("error", "write error", exception.getMessage());
        }

        @Override
        public Map<String, List<String>> getConnectionHeaders() {
            return mHeaders;
        }
    }

    public void cleanup() {
        // Cleanup socket.io
        mEngineIoServer.shutdown();
        // Cleanup project reactor schedulers
        Schedulers.shutdownNow();
    }

}
