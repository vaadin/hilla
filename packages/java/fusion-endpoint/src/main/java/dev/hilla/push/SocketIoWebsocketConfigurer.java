package dev.hilla.push;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Sets up a websocket endpoint for the socket io server.
 */
@Configuration
@EnableWebSocket
public class SocketIoWebsocketConfigurer implements WebSocketConfigurer {

    private final EngineIoHandler engineIoHandler;

    /**
     * Creates the websocket configurer.
     *
     * @param engineIoHandler
     *            the engine io handler
     */
    public SocketIoWebsocketConfigurer(EngineIoHandler engineIoHandler) {
        this.engineIoHandler = engineIoHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(engineIoHandler, "/VAADIN/hillapush/")
                .addInterceptors(engineIoHandler);
    }

}