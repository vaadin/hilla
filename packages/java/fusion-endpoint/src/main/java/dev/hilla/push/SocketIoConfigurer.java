package dev.hilla.push;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.hilla.EndpointInvoker;
import io.socket.engineio.server.EngineIoServer;
import io.socket.socketio.server.SocketIoServer;

/**
 * Defines the beans needed for push in Hilla.
 */
@Configuration
public class SocketIoConfigurer {

    @Bean
    public PushMessageHandler pushMessageHandler(
            EndpointInvoker endpointInvoker) {
        return new PushMessageHandler(endpointInvoker);
    }

    @Bean
    public EngineIoServer engineIoServer() {
        return new EngineIoServer();
    }

    @Bean
    public SocketIoServer socketIoServer(EngineIoServer engineIoServer) {
        return new SocketIoServer(engineIoServer);
    }

    @Bean
    public SocketIoHandler socketIoHandler(SocketIoServer socketIoServer,
            ObjectMapper objectMapper, PushMessageHandler pushMessageHandler) {
        return new SocketIoHandler(socketIoServer, objectMapper,
                pushMessageHandler);
    }
}
