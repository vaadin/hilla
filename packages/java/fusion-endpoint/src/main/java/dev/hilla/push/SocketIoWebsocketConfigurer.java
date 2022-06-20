package dev.hilla.push;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import dev.hilla.ConditionalOnFeatureFlag;

/**
 * Sets up a websocket endpoint for the socket io server.
 */
@Configuration
@EnableWebSocket
@ConditionalOnFeatureFlag(PushMessageHandler.PUSH_FEATURE_FLAG)
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
        registry.addHandler(engineIoHandler, "/HILLA/push")
                .addInterceptors(engineIoHandler);
    }

}
