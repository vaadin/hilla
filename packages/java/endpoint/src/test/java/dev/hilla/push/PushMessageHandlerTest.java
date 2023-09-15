package dev.hilla.push;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hilla.EndpointController;
import dev.hilla.EndpointControllerConfiguration;
import dev.hilla.EndpointInvocationException.EndpointAccessDeniedException;
import dev.hilla.EndpointInvocationException.EndpointBadRequestException;
import dev.hilla.EndpointInvocationException.EndpointInternalException;
import dev.hilla.EndpointInvocationException.EndpointNotFoundException;
import dev.hilla.EndpointInvoker;
import dev.hilla.EndpointProperties;
import dev.hilla.EndpointSubscription;
import dev.hilla.ServletContextTestSetup;
import dev.hilla.push.PushMessageHandler.SubscriptionInfo;
import dev.hilla.push.messages.fromclient.SubscribeMessage;
import dev.hilla.push.messages.fromclient.UnsubscribeMessage;
import dev.hilla.push.messages.toclient.AbstractClientMessage;
import dev.hilla.push.messages.toclient.ClientMessageComplete;
import dev.hilla.push.messages.toclient.ClientMessageError;
import dev.hilla.push.messages.toclient.ClientMessageUpdate;
import net.jcip.annotations.NotThreadSafe;
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
import reactor.core.publisher.Flux;

@SpringBootTest(classes = { PushMessageHandler.class,
        ServletContextTestSetup.class, EndpointProperties.class,
        Jackson2ObjectMapperBuilder.class, JacksonProperties.class,
        PushMessageHandler.class, ObjectMapper.class,
        EndpointController.class })
@ContextConfiguration(classes = { EndpointControllerConfiguration.class })
@RunWith(SpringRunner.class)
@TestPropertySource(properties = "dev.hilla.FeatureFlagCondition.alwaysEnable=true")
@NotThreadSafe
public class PushMessageHandlerTest {

    private static final String ENDPOINT_NAME = "TestEndpoint";
    private static final String FLUX_METHOD = "testFlux";
    private static final String INFINITE_FLUX_METHOD = "testInfiniteFlux";
    private static final String FLUX_WITH_EXCEPTION_METHOD = "testFluxWithException";
    private static final String ENDPOINT_SUBSCRIPTION_METHOD = "testEndpointSubscription";
    private static final String INFINITE_ENDPOINT_SUBSCRIPTION_METHOD = "testInfiniteEndpointSubscription";
    private static final String ENDPOINT_SUBSCRIPTION_WITH_EXCEPTION_METHOD = "testEndpointSubscriptionWithException";

    @Autowired
    private PushMessageHandler pushMessageHandler;

    @MockBean
    private EndpointInvoker endpointInvoker;

    @Autowired
    private ObjectMapper objectMapper;
    private int unsubscribeCalled;
    private Runnable unsubscribeHandler = () -> {
        unsubscribeCalled++;
    };
    private List<AbstractClientMessage> unexpectedMessages = new ArrayList<>();

    private String connectionId;

    @Before
    public void setup()
            throws EndpointNotFoundException, EndpointAccessDeniedException,
            EndpointBadRequestException, EndpointInternalException {
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
                    } else if (methodName.equals(ENDPOINT_SUBSCRIPTION_METHOD)
                            || methodName.equals(
                                    ENDPOINT_SUBSCRIPTION_WITH_EXCEPTION_METHOD)
                            || methodName.equals(
                                    INFINITE_ENDPOINT_SUBSCRIPTION_METHOD)) {
                        return EndpointSubscription.class;
                    }

                    return null;
                });

        unsubscribeCalled = 0;
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
                    } else if (methodName
                            .equals(ENDPOINT_SUBSCRIPTION_METHOD)) {
                        return EndpointSubscription.of(createSingleDataFlux(),
                                unsubscribeHandler);
                    } else if (methodName
                            .equals(INFINITE_ENDPOINT_SUBSCRIPTION_METHOD)) {
                        return EndpointSubscription.of(createInfiniteDataFlux(),
                                unsubscribeHandler);
                    } else if (methodName.equals(
                            ENDPOINT_SUBSCRIPTION_WITH_EXCEPTION_METHOD)) {
                        return EndpointSubscription.of(createErrorFlux(),
                                unsubscribeHandler);
                    }
                    return null;
                });

        connectionId = UUID.randomUUID().toString();
        pushMessageHandler.fluxSubscriptionInfos = new ConcurrentHashMap<>();
        pushMessageHandler.handleBrowserConnect(connectionId);
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
        Assert.assertEquals(0, pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId).size());
        SubscribeMessage message = createInfiniteFluxSubscribe();
        pushMessageHandler.handleMessage(connectionId, message,
                ignoreUpdateMessages());
        Assert.assertEquals(1, pushMessageHandler.fluxSubscriptionInfos.size());
        Assert.assertEquals(1, pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId).size());
    }

    @Test
    public void fluxSubscription_receivesMessage() throws Exception {
        SubscribeMessage subscribeMessage = createFluxSubscribe();
        CompletableFuture<ClientMessageUpdate> clientMessageWrapper = new CompletableFuture<>();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageUpdate) {
                        clientMessageWrapper
                                .complete((ClientMessageUpdate) msg);
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
    public void endpointSubscription_receivesMessage() throws Exception {
        SubscribeMessage subscribeMessage = createFluxSubscribe();
        CompletableFuture<ClientMessageUpdate> clientMessageWrapper = new CompletableFuture<>();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageUpdate) {
                        clientMessageWrapper
                                .complete((ClientMessageUpdate) msg);
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
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageUpdate) {
                        // Ignore for this test
                    } else if (msg instanceof ClientMessageComplete) {
                        clientMessageWrapper
                                .complete((ClientMessageComplete) msg);
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
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
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

    @Test
    public void fluxSubscription_cleanUpProperlyOnImmediateExceptionInFlux()
            throws Exception {
        SubscribeMessage subscribeMessage = createFluxWithExceptionSubscribe();
        CountDownLatch wait = new CountDownLatch(1);

        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageError) {
                        wait.countDown();
                    } else {
                        unexpectedMessages.add(msg);
                    }
                });

        wait.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId).size());
    }

    @Test
    public void fluxSubscription_cleanUpProperlyOnImmediateFluxComplete()
            throws Exception {
        SubscribeMessage subscribeMessage = createFluxSubscribe();
        CountDownLatch wait = new CountDownLatch(1);

        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageComplete) {
                        wait.countDown();
                    }
                });

        wait.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId).size());
    }

    @Test
    public void fluxSubscription_browserUnsubscribesCleansUp()
            throws Exception {
        SubscribeMessage subscribeMessage = createInfiniteFluxSubscribe();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                ignoreUpdateMessages());

        Assert.assertEquals(1, pushMessageHandler.fluxSubscriptionInfos.size());

        UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage();
        unsubscribeMessage.setId(subscribeMessage.getId());
        pushMessageHandler.handleMessage(connectionId, unsubscribeMessage,
                ignoreAll());
        Assert.assertEquals(List.of(), unexpectedMessages);
        Assert.assertEquals(1, pushMessageHandler.fluxSubscriptionInfos.size());
        Assert.assertTrue(pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId).isEmpty());
    }

    @Test
    public void fluxSubscription_browserDisconnectCleansUp() throws Exception {
        SubscribeMessage subscribeMessage = createInfiniteFluxSubscribe();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                ignoreUpdateMessages());
        SubscribeMessage subscribeMessage2 = createInfiniteFluxSubscribe();
        subscribeMessage2.setId("2");
        pushMessageHandler.handleMessage(connectionId, subscribeMessage2,
                ignoreUpdateMessages());

        Assert.assertEquals(1, pushMessageHandler.fluxSubscriptionInfos.size());
        ConcurrentHashMap<String, SubscriptionInfo> subscriptions = pushMessageHandler.fluxSubscriptionInfos
                .get(connectionId);
        Assert.assertEquals(2, subscriptions.size());

        pushMessageHandler.handleBrowserDisconnect(connectionId);
        Assert.assertEquals(0, pushMessageHandler.fluxSubscriptionInfos.size());
    }

    @Test
    public void endpointSubscription_triggersUnsubscribeCallbackOnClientCloseMessage()
            throws Exception {
        SubscribeMessage subscribeMessage = createInfiniteEndpointSubscriptionSubscribe();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                ignore(ClientMessageUpdate.class));

        UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage();
        unsubscribeMessage.setId(subscribeMessage.getId());
        Assert.assertEquals(0, unsubscribeCalled);
        pushMessageHandler.handleMessage(connectionId, unsubscribeMessage,
                ignoreAll());
        Assert.assertEquals(1, unsubscribeCalled);
    }

    @Test
    public void endpointSubscription_triggersUnsubscribeCallbackOnClientConnectionLost()
            throws Exception {
        SubscribeMessage subscribeMessage = createInfiniteEndpointSubscriptionSubscribe();
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                ignoreUpdateMessages());

        Assert.assertEquals(0, unsubscribeCalled);
        pushMessageHandler.handleBrowserDisconnect(connectionId);
        Assert.assertEquals(1, unsubscribeCalled);
    }

    @Test
    public void endpointSubscription_doesNotTriggerUnsubscribeCallbackOnFluxException()
            throws Exception {
        SubscribeMessage subscribeMessage = createEndpointSubscriptionWithExceptionSubscribe();
        CountDownLatch done = new CountDownLatch(1);
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageError) {
                        done.countDown();
                    }
                });
        done.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, unsubscribeCalled);
    }

    @Test
    public void endpointSubscription_doesNotTriggerUnsubscribeCallbackOnFluxCompletion()
            throws Exception {
        SubscribeMessage subscribeMessage = createEndpointSubscriptionSubscribe();
        CountDownLatch done = new CountDownLatch(1);
        pushMessageHandler.handleMessage(connectionId, subscribeMessage,
                msg -> {
                    if (msg instanceof ClientMessageComplete) {
                        done.countDown();
                    }
                });
        done.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, unsubscribeCalled);
    }

    private Consumer<AbstractClientMessage> ignoreAll() {
        return msg -> {
        };
    }

    private Consumer<AbstractClientMessage> ignoreUpdateMessages() {
        return ignore(ClientMessageUpdate.class);
    }

    private Consumer<AbstractClientMessage> ignore(Class<?>... toIgnore) {
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
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(FLUX_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createInfiniteFluxSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(INFINITE_FLUX_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createFluxWithExceptionSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(FLUX_WITH_EXCEPTION_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createEndpointSubscriptionSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(ENDPOINT_SUBSCRIPTION_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createInfiniteEndpointSubscriptionSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage.setMethodName(INFINITE_ENDPOINT_SUBSCRIPTION_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }

    private SubscribeMessage createEndpointSubscriptionWithExceptionSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(connectionId);
        subscribeMessage.setEndpointName(ENDPOINT_NAME);
        subscribeMessage
                .setMethodName(ENDPOINT_SUBSCRIPTION_WITH_EXCEPTION_METHOD);
        subscribeMessage.setParams(objectMapper.createArrayNode());
        return subscribeMessage;
    }
}
