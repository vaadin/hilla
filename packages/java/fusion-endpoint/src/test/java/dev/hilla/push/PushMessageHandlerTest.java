package dev.hilla.push;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.server.VaadinServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import dev.hilla.EndpointControllerConfiguration;
import dev.hilla.EndpointInvoker;
import dev.hilla.EndpointProperties;
import dev.hilla.ServletContextTestSetup;
import dev.hilla.push.messages.fromclient.SubscribeMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;
import dev.hilla.push.messages.toclient.ClientMessageComplete;
import dev.hilla.push.messages.toclient.ClientMessageError;
import dev.hilla.push.messages.toclient.ClientMessageUpdate;
import reactor.core.publisher.Flux;

@SpringBootTest(classes = { PushMessageHandler.class,
        ServletContextTestSetup.class, EndpointProperties.class,
        Jackson2ObjectMapperBuilder.class, JacksonProperties.class,
        PushMessageHandler.class, ObjectMapper.class })
@ContextConfiguration(classes = EndpointControllerConfiguration.class)
@RunWith(SpringRunner.class)
@TestPropertySource(properties = "dev.hilla.FeatureFlagCondition.alwaysEnable=true")
public class PushMessageHandlerTest {

    private static final String ENDPOINT_NAME = "TestEndpoint";
    private static final String FLUX_METHOD = "testFlux";
    private static final String INFINITE_FLUX_METHOD = "testInfiniteFlux";
    private static final String FLUX_WITH_EXCEPTION_METHOD = "testFluxWithException";

    private static final String CONNECTION_ID = "cid";

    private static final Consumer<AbstractClientMessage> IGNORE = msg -> {
    };

    @Autowired
    private PushMessageHandler pushMessageHandler;

    @MockBean
    private EndpointInvoker endpointInvoker;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ObjectMapper objectMapper;
    private List<AbstractClientMessage> unexpectedMessages = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        FeatureFlags featureFlags = FeatureFlags
                .get(new VaadinServletContext(servletContext));
        try {
            featureFlags.setEnabled(FeatureFlags.HILLA_PUSH.getId(), true);
        } catch (Exception e) {
            // Ignore that the file cannot be saved
        }

        Mockito.when(endpointInvoker.getReturnType(Mockito.anyString(),
                Mockito.anyString())).thenAnswer(request -> {
                    if (!request.getArgument(0).equals(ENDPOINT_NAME)) {
                        return null;
                    }
                    String methodName = request.getArgument(1);
                    if (methodName.equals(FLUX_METHOD)
                            || methodName.equals(FLUX_WITH_EXCEPTION_METHOD)
                            || methodName.equals(INFINITE_FLUX_METHOD)) {
                        return Flux.class;
                    }

                    return null;
                });

        Mockito.when(endpointInvoker.invoke(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(request -> {
                    if (!request.getArgument(0).equals(ENDPOINT_NAME)) {
                        return null;
                    }
                    String methodName = request.getArgument(1);
                    if (methodName.equals(FLUX_METHOD)) {
                        return createSingleDataFlux();
                    } else if (methodName.equals(INFINITE_FLUX_METHOD)) {
                        return createInfiniteDataFlux();
                    } else if (methodName.equals(FLUX_WITH_EXCEPTION_METHOD)) {
                        return createErrorFlux();
                    }
                    return null;
                });
    }

    @After
    public void after() {
        Assert.assertEquals(List.of(), unexpectedMessages);
    }

    private Flux<String> createSingleDataFlux() {
        return Flux.just("Hello");
    }

    private Flux<Long> createInfiniteDataFlux() {
        return Flux.interval(Duration.ofMillis(500));
    }

    private Flux<Object> createErrorFlux() {
        return Flux.error(new RuntimeException("Intentional error"));
    }

    @Test
    public void fluxSubscription_canSubscribe() {
        Assert.assertEquals(0, pushMessageHandler.closeHandlers.size());
        SubscribeMessage message = createInfiniteFluxSubscribe();
        pushMessageHandler.handleMessage(message,
                ignore(ClientMessageUpdate.class));
        Assert.assertEquals(1, pushMessageHandler.closeHandlers.size());
    }

    @Test
    public void fluxSubscription_receivesMessage() throws Exception {
        SubscribeMessage subscribeMessage = createFluxSubscribe();
        CompletableFuture<ClientMessageUpdate> clientMessageWrapper = new CompletableFuture<>();
        pushMessageHandler.handleMessage(subscribeMessage, msg -> {
            if (msg instanceof ClientMessageUpdate) {
                clientMessageWrapper.complete((ClientMessageUpdate) msg);
            } else if (msg instanceof ClientMessageComplete) {
                // Expected
            } else {
                unexpectedMessages.add(msg);
            }
        });

        ClientMessageUpdate clientMessage = clientMessageWrapper.get(2,
                TimeUnit.SECONDS);
        Assert.assertEquals(subscribeMessage.getId(), clientMessage.getId());
        Assert.assertEquals("Hello", clientMessage.getItem());
    }

    @Test
    public void fluxSubscription_completeMessageDeliveredToClient()
            throws Exception {
        SubscribeMessage subscribeMessage = createFluxSubscribe();
        CompletableFuture<ClientMessageComplete> clientMessageWrapper = new CompletableFuture<>();
        pushMessageHandler.handleMessage(subscribeMessage, msg -> {
            if (msg instanceof ClientMessageUpdate) {
                // Ignore for this test
            } else if (msg instanceof ClientMessageComplete) {
                clientMessageWrapper.complete((ClientMessageComplete) msg);
            } else {
                unexpectedMessages.add(msg);
            }
        });

        ClientMessageComplete clientMessage = clientMessageWrapper.get(2,
                TimeUnit.SECONDS);
        Assert.assertEquals(subscribeMessage.getId(), clientMessage.getId());
    }

    @Test
    public void fluxSubscription_exceptionDeliveredToClient() throws Exception {
        SubscribeMessage subscribeMessage = createFluxWithExceptionSubscribe();
        CompletableFuture<ClientMessageError> clientMessageWrapper = new CompletableFuture<>();
        pushMessageHandler.handleMessage(subscribeMessage, msg -> {
            if (msg instanceof ClientMessageError) {
                clientMessageWrapper.complete((ClientMessageError) msg);
            } else {
                unexpectedMessages.add(msg);
            }
        });

        ClientMessageError clientMessage = clientMessageWrapper.get(2,
                TimeUnit.SECONDS);
        Assert.assertEquals(subscribeMessage.getId(), clientMessage.getId());
        Assert.assertEquals("Exception in Flux", clientMessage.getMessage());
    }

    private Consumer<AbstractClientMessage> ignore(Class<?>... toIgnore) {
        List<AbstractClientMessage> unexpectedMessages = new ArrayList<>();
        return msg -> {
            for (Class<?> c : toIgnore) {
                if (c == msg.getClass()) {
                    return;
                }
            }

            unexpectedMessages.add(msg);
        };
    }

    private SubscribeMessage createFluxSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(CONNECTION_ID);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(FLUX_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createInfiniteFluxSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(CONNECTION_ID);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(INFINITE_FLUX_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createFluxWithExceptionSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(CONNECTION_ID);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(FLUX_WITH_EXCEPTION_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

}
