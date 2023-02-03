package dev.hilla;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.shared.ApplicationConstants;

import dev.hilla.auth.CsrfChecker;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.exception.EndpointException;
import dev.hilla.exception.EndpointValidationException;
import dev.hilla.generator.endpoints.iterableendpoint.IterableEndpoint;
import dev.hilla.generator.endpoints.superclassmethods.PersonEndpoint;
import dev.hilla.parser.jackson.JacksonObjectMapperFactory;
import dev.hilla.testendpoint.BridgeMethodTestEndpoint;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EndpointControllerTest {
    private static final TestClass TEST_ENDPOINT = new TestClass();
    private static final String TEST_ENDPOINT_NAME = TEST_ENDPOINT.getClass()
            .getSimpleName();
    private static final Method TEST_METHOD;
    private static final Method TEST_VALIDATION_METHOD;
    private HttpServletRequest requestMock;
    private Principal principal;
    private ApplicationConfiguration appConfig;

    static {
        TEST_METHOD = Stream.of(TEST_ENDPOINT.getClass().getDeclaredMethods())
                .filter(method -> "testMethod".equals(method.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Failed to find a test endpoint method"));
        TEST_VALIDATION_METHOD = Stream
                .of(TEST_ENDPOINT.getClass().getDeclaredMethods())
                .filter(method -> "testValidationMethod"
                        .equals(method.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Failed to find a test validation endpoint method"));
    }

    private static class TestValidationParameter {
        @Min(10)
        private final int count;

        public TestValidationParameter(@JsonProperty("count") int count) {
            this.count = count;
        }
    }

    @Endpoint
    public static class TestClass {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }

        public void testValidationMethod(
                @NotNull TestValidationParameter parameter) {
            // no op
        }

        public void testMethodWithMultipleParameter(int number, String text,
                Date date) {
            // no op
        }

        @AnonymousAllowed
        public String testAnonymousMethod() {
            return "Hello, anonymous user!";
        }

        @PermitAll
        @RolesAllowed({ "FOO_ROLE", "BAR_ROLE" })
        public String testRoleAllowed() {
            return "Hello, user in role!";
        }

        @DenyAll
        @AnonymousAllowed
        public void denyAll() {
        }

        @RolesAllowed("FOO_ROLE")
        @AnonymousAllowed
        public String anonymousOverrides() {
            return "Hello, no user!";
        }

        @PermitAll
        public String getUserName() {
            return VaadinService.getCurrentRequest().getUserPrincipal()
                    .getName();
        }
    }

    @Endpoint("CustomEndpoint")
    public static class TestClassWithCustomEndpointName {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }
    }

    @Endpoint("my endpoint")
    public static class TestClassWithIllegalEndpointName {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }
    }

    @Endpoint
    public static class NullCheckerTestClass {
        public static final String OK_RESPONSE = "ok";

        public String testOkMethod() {
            return OK_RESPONSE;
        }

        public String testNullMethod() {
            return null;
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        requestMock = mock(HttpServletRequest.class);
        principal = mock(Principal.class);

        appConfig = Mockito.mock(ApplicationConfiguration.class);

        when(requestMock.getUserPrincipal()).thenReturn(principal);
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn("Vaadin Fusion");
        doReturn(mockServletContext()).when(requestMock).getServletContext();

        when(requestMock.getCookies()).thenReturn(new Cookie[] {
                new Cookie(ApplicationConstants.CSRF_TOKEN, "Vaadin Fusion") });
    }

    @Test
    public void should_ThrowException_When_NoEndpointNameCanBeReceived() {
        TestClass anonymousClass = new TestClass() {
        };
        assertEquals("Endpoint to test should have no name",
                anonymousClass.getClass().getSimpleName(), "");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("anonymous");
        exception.expectMessage(anonymousClass.getClass().getName());
        createVaadinController(anonymousClass);
    }

    @Test
    public void should_ThrowException_When_IncorrectEndpointNameProvided() {
        TestClassWithIllegalEndpointName endpointWithIllegalName = new TestClassWithIllegalEndpointName();
        String incorrectName = endpointWithIllegalName.getClass()
                .getAnnotation(Endpoint.class).value();
        EndpointNameChecker nameChecker = new EndpointNameChecker();
        String expectedCheckerMessage = nameChecker.check(incorrectName);
        assertNotNull(expectedCheckerMessage);

        exception.expect(IllegalStateException.class);
        exception.expectMessage(incorrectName);
        exception.expectMessage(expectedCheckerMessage);

        createVaadinController(endpointWithIllegalName,
                mock(JacksonObjectMapperFactory.class), null, nameChecker, null,
                null);
    }

    @Test
    public void should_Return404_When_EndpointNotFound() {
        String missingEndpointName = "whatever";
        assertNotEquals(missingEndpointName, TEST_ENDPOINT_NAME);

        ResponseEntity<?> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(missingEndpointName, null, null, requestMock);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void should_Return404_When_MethodNotFound() {
        String missingEndpointMethod = "whatever";
        assertNotEquals(TEST_METHOD.getName(), missingEndpointMethod);

        ResponseEntity<?> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, missingEndpointMethod, null,
                        requestMock);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void should_Return404_When_IllegalAccessToMethodIsPerformed() {
        String accessErrorMessage = "Access error";

        EndpointAccessChecker restrictingCheckerMock = mock(
                EndpointAccessChecker.class);
        when(restrictingCheckerMock.check(Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(accessErrorMessage);

        EndpointNameChecker nameCheckerMock = mock(EndpointNameChecker.class);
        when(nameCheckerMock.check(TEST_ENDPOINT_NAME)).thenReturn(null);

        ExplicitNullableTypeChecker explicitNullableTypeCheckerMock = mock(
                ExplicitNullableTypeChecker.class);

        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT,
                new JacksonObjectMapperFactory.Json(), restrictingCheckerMock,
                nameCheckerMock, explicitNullableTypeCheckerMock, null)
                        .serveEndpoint(TEST_ENDPOINT_NAME,
                                TEST_METHOD.getName(), null, requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(accessErrorMessage));

        verify(restrictingCheckerMock).check(Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.verifyNoMoreInteractions(restrictingCheckerMock);
        verify(restrictingCheckerMock, times(1)).check(Mockito.any(),
                Mockito.any(), Mockito.any());
    }

    @Test
    public void should_Return400_When_LessParametersSpecified1() {
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(), null,
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("0"));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        Integer.toString(TEST_METHOD.getParameterCount())));
    }

    @Test
    public void should_Return400_When_MoreParametersSpecified() {
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestParameters(
                                "{\"value1\": 222, \"value2\": 333}"),
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("2"));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        Integer.toString(TEST_METHOD.getParameterCount())));
    }

    @Test
    public void should_Return400_When_IncorrectParameterTypesAreProvided() {
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestParameters("{\"value\": [222]}"),
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        TEST_METHOD.getParameterTypes()[0].getSimpleName()));
    }

    @Test
    public void should_NotCallMethod_When_UserPrincipalIsNull() {
        EndpointController vaadinController = createVaadinControllerWithoutPrincipal();
        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters("{\"value\": 222}"), requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull("Response body should not be null", responseBody);
        assertTrue("Should return unauthorized error",
                responseBody.contains(EndpointAccessChecker.ACCESS_DENIED_MSG));
    }

    @Test
    public void should_CallMethodAnonymously_When_UserPrincipalIsNullAndAnonymousAllowed() {
        EndpointController vaadinController = createVaadinControllerWithoutPrincipal();
        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testAnonymousMethod",
                createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        assertEquals("Should return message when calling anonymously",
                "\"Hello, anonymous user!\"", responseBody);
    }

    @Test
    public void should_NotCallMethod_When_a_CSRF_request() {
        when(appConfig.isXsrfProtectionEnabled()).thenReturn(true);
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn(null);

        EndpointController vaadinController = createVaadinControllerWithoutPrincipal();
        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testAnonymousMethod",
                createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull("Response body should not be null", responseBody);
        assertTrue("Should return unauthorized error",
                responseBody.contains(EndpointAccessChecker.ACCESS_DENIED_MSG));
    }

    @Test
    public void should_NotCallMethodAnonymously_When_UserPrincipalIsNotInRole() {
        EndpointController vaadinController = createVaadinController(
                TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));

        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testRoleAllowed",
                createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody()
                .contains(EndpointAccessChecker.ACCESS_DENIED_MSG));
    }

    @Test
    public void should_CallMethodAnonymously_When_UserPrincipalIsInRole() {
        when(requestMock.isUserInRole("FOO_ROLE")).thenReturn(true);

        EndpointController vaadinController = createVaadinController(
                TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));

        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testRoleAllowed",
                createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals("\"Hello, user in role!\"", response.getBody());
    }

    @Test
    public void should_CallMethodAnonymously_When_AnonymousOverridesRoles() {
        EndpointController vaadinController = createVaadinController(
                TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));

        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "anonymousOverrides",
                createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"Hello, no user!\"", response.getBody());
    }

    @Test
    public void should_NotCallMethod_When_DenyAll() {
        EndpointController vaadinController = createVaadinControllerWithoutPrincipal();
        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "denyAll", createRequestParameters("{}"),
                requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody()
                .contains(EndpointAccessChecker.ACCESS_DENIED_MSG));
    }

    @Test
    public void should_bePossibeToGetPrincipalInEndpoint() {
        when(principal.getName()).thenReturn("foo");

        EndpointController vaadinController = createVaadinController(
                TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));

        ResponseEntity<String> response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "getUserName",
                createRequestParameters("{}"), requestMock);

        assertEquals("\"foo\"", response.getBody());
    }

    @Test
    public void should_clearVaadinRequestInsntace_after_EndpointCall() {
        EndpointController vaadinController = createVaadinController(
                TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));

        vaadinController.serveEndpoint(TEST_ENDPOINT_NAME, "getUserName",
                createRequestParameters("{}"), requestMock);

        Assert.assertNull(CurrentInstance.get(VaadinRequest.class));
        Assert.assertNull(VaadinRequest.getCurrent());
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsIllegalArgumentException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(
                inputValue, new IllegalArgumentException("OOPS"));

        EndpointController controller = createVaadinController(TEST_ENDPOINT);
        controller.endpointRegistry
                .get(TEST_ENDPOINT_NAME.toLowerCase()).methods.put(
                        TEST_METHOD.getName().toLowerCase(),
                        endpointMethodMock);

        ResponseEntity<String> response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        TEST_METHOD.getParameterTypes()[0].getSimpleName()));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return500_When_EndpointMethodThrowsIllegalAccessException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(
                inputValue, new IllegalAccessException("OOPS"));

        EndpointController controller = createVaadinController(TEST_ENDPOINT);
        controller.endpointRegistry
                .get(TEST_ENDPOINT_NAME.toLowerCase()).methods.put(
                        TEST_METHOD.getName().toLowerCase(),
                        endpointMethodMock);

        ResponseEntity<String> response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("access failure"));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return500_When_EndpointMethodThrowsInvocationTargetException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(
                inputValue, new InvocationTargetException(
                        new IllegalStateException("OOPS")));

        EndpointController controller = createVaadinController(TEST_ENDPOINT);
        controller.endpointRegistry
                .get(TEST_ENDPOINT_NAME.toLowerCase()).methods.put(
                        TEST_METHOD.getName().toLowerCase(),
                        endpointMethodMock);

        ResponseEntity<String> response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("execution failure"));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsVaadinConnectException()
            throws Exception {
        int inputValue = 222;
        String expectedMessage = "OOPS";

        Method endpointMethodMock = createEndpointMethodMockThatThrows(
                inputValue, new InvocationTargetException(
                        new EndpointException(expectedMessage)));

        EndpointController controller = createVaadinController(TEST_ENDPOINT);
        controller.endpointRegistry
                .get(TEST_ENDPOINT_NAME.toLowerCase()).methods.put(
                        TEST_METHOD.getName().toLowerCase(),
                        endpointMethodMock);

        ResponseEntity<String> response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(EndpointException.class.getName()));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(expectedMessage));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsVaadinConnectExceptionSubclass()
            throws Exception {
        int inputValue = 222;
        String expectedMessage = "OOPS";

        class MyCustomException extends EndpointException {
            public MyCustomException() {
                super(expectedMessage);
            }
        }

        Method endpointMethodMock = createEndpointMethodMockThatThrows(
                inputValue,
                new InvocationTargetException(new MyCustomException()));

        EndpointController controller = createVaadinController(TEST_ENDPOINT);
        controller.endpointRegistry
                .get(TEST_ENDPOINT_NAME.toLowerCase()).methods.put(
                        TEST_METHOD.getName().toLowerCase(),
                        endpointMethodMock);

        ResponseEntity<String> response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(MyCustomException.class.getName()));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(expectedMessage));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    public void should_ReturnCorrectResponse_When_EverythingIsCorrect() {
        int inputValue = 222;
        String expectedOutput = TEST_ENDPOINT.testMethod(inputValue);

        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestParameters(
                                String.format("{\"value\": %s}", inputValue)),
                        requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
                response.getBody());
    }

    @Test
    public void should_ReturnCorrectResponse_When_EndpointClassIsProxied() {

        ApplicationContext contextMock = mock(ApplicationContext.class);
        TestClass endpoint = new TestClass();

        // CGLib proxies are supported as entry-point classes
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TestClass.class);
        enhancer.setCallback(NoOp.INSTANCE);
        TestClass proxy = (TestClass) enhancer.create();

        when(contextMock.getBeansWithAnnotation(Endpoint.class))

                .thenReturn(Collections.singletonMap(
                        endpoint.getClass().getSimpleName(), proxy));

        EndpointController fusionController = createVaadinControllerWithApplicationContext(
                contextMock);

        int inputValue = 222;
        String expectedOutput = endpoint.testMethod(inputValue);

        ResponseEntity<String> response = fusionController.serveEndpoint(
                "TestClass", "testMethod",
                createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)),
                requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
                response.getBody());
    }

    @Test
    public void should_NotUseBridgeMethod_When_EndpointHasBridgeMethodFromInterface() {
        String inputId = "2222";
        String expectedResult = String.format("{\"id\":\"%s\"}", inputId);
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testMethodFromInterface";
        ResponseEntity<String> response = createVaadinController(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName,
                        createRequestParameters(String.format(
                                "{\"value\": {\"id\": \"%s\"}}", inputId)),
                        requestMock);
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    public void should_NotUseBridgeMethod_When_EndpointHasBridgeMethodFromParentClass() {
        String inputId = "2222";
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testMethodFromClass";

        ResponseEntity<String> response = createVaadinController(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName,
                        createRequestParameters(
                                String.format("{\"value\": %s}", inputId)),
                        requestMock);
        assertEquals(inputId, response.getBody());
    }

    @Test
    public void should_ReturnCorrectResponse_When_CallingNormalOverriddenMethod() {
        String inputId = "2222";
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testNormalMethod";

        ResponseEntity<String> response = createVaadinController(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName,
                        createRequestParameters(
                                String.format("{\"value\": %s}", inputId)),
                        requestMock);
        assertEquals(inputId, response.getBody());
    }

    @Test
    public void should_UseCustomEndpointName_When_ItIsDefined() {
        int input = 111;
        String expectedOutput = new TestClassWithCustomEndpointName()
                .testMethod(input);
        String beanName = TestClassWithCustomEndpointName.class.getSimpleName();

        ApplicationContext contextMock = mock(ApplicationContext.class);
        when(contextMock.getBeansWithAnnotation(Endpoint.class))
                .thenReturn(Collections.singletonMap(beanName,
                        new TestClassWithCustomEndpointName()));

        EndpointController fusionController = createVaadinControllerWithApplicationContext(
                contextMock);

        ResponseEntity<String> response = fusionController
                .serveEndpoint("CustomEndpoint", "testMethod",
                        createRequestParameters(
                                String.format("{\"value\": %s}", input)),
                        requestMock);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
                response.getBody());
    }

    @Test
    public void should_UseCustomEndpointName_When_EndpointClassIsProxied() {

        ApplicationContext contextMock = mock(ApplicationContext.class);
        TestClassWithCustomEndpointName endpoint = new TestClassWithCustomEndpointName();
        TestClassWithCustomEndpointName proxy = mock(
                TestClassWithCustomEndpointName.class, CALLS_REAL_METHODS);
        when(contextMock.getBeansWithAnnotation(Endpoint.class)).thenReturn(
                Collections.singletonMap(endpoint.getClass().getSimpleName(),
                        proxy));

        EndpointController fusionController = createVaadinControllerWithApplicationContext(
                contextMock);

        int input = 111;
        String expectedOutput = endpoint.testMethod(input);

        ResponseEntity<String> response = fusionController
                .serveEndpoint("CustomEndpoint", "testMethod",
                        createRequestParameters(
                                String.format("{\"value\": %s}", input)),
                        requestMock);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
                response.getBody());
    }

    @Test
    public void should_Never_UseSpringObjectMapper() {
        ApplicationContext contextMock = mock(ApplicationContext.class);
        ObjectMapper mockSpringObjectMapper = mock(ObjectMapper.class);
        ObjectMapper mockOwnObjectMapper = mock(ObjectMapper.class);
        Jackson2ObjectMapperBuilder mockObjectMapperBuilder = mock(
                Jackson2ObjectMapperBuilder.class);
        JacksonProperties mockJacksonProperties = mock(JacksonProperties.class);
        when(contextMock.getBean(ObjectMapper.class))
                .thenReturn(mockSpringObjectMapper);
        when(contextMock.getBean(JacksonProperties.class))
                .thenReturn(mockJacksonProperties);
        when(contextMock.getBean(Jackson2ObjectMapperBuilder.class))
                .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.createXmlMapper(false))
                .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.build()).thenReturn(mockOwnObjectMapper);
        when(mockJacksonProperties.getVisibility())
                .thenReturn(Collections.emptyMap());
        EndpointRegistry registry = new EndpointRegistry(
                mock(EndpointNameChecker.class));

        EndpointInvoker invoker = new EndpointInvoker(contextMock, null,
                mock(ExplicitNullableTypeChecker.class),
                mock(ServletContext.class), registry);
        new EndpointController(contextMock, registry, invoker, null);

        verify(contextMock, never()).getBean(ObjectMapper.class);
        verify(contextMock, times(1))
                .getBean(Jackson2ObjectMapperBuilder.class);
    }

    @Test
    public void should_ReturnValidationError_When_DeserializationFails()
            throws IOException {
        String inputValue = "\"string\"";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_METHOD.getName());
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestParameters(
                                String.format("{\"value\": %s}", inputValue)),
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertEquals("value",
                validationErrorData.get("parameterName").asText());
        assertTrue(
                validationErrorData.get("message").asText().contains("'int'"));
    }

    @Test
    public void should_ReturnAllValidationErrors_When_DeserializationFailsForMultipleParameters()
            throws IOException {
        String inputValue = String.format(
                "{\"number\": %s, \"text\": %s, \"date\": %s}",
                "\"NotANumber\"", "\"ValidText\"", "\"NotADate\"");
        String testMethodName = "testMethodWithMultipleParameter";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, testMethodName);
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, testMethodName,
                        createRequestParameters(inputValue), requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);
        assertNotNull(jsonNodes);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(2, jsonNodes.get("validationErrorData").size());

        List<String> parameterNames = jsonNodes.get("validationErrorData")
                .findValuesAsText("parameterName");
        assertEquals(2, parameterNames.size());
        assertTrue(parameterNames.contains("date"));
        assertTrue(parameterNames.contains("number"));
    }

    @Test
    public void should_ReturnValidationError_When_EndpointMethodParameterIsInvalid()
            throws IOException {
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_VALIDATION_METHOD.getName());

        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME,
                        TEST_VALIDATION_METHOD.getName(),
                        createRequestParameters("{\"parameter\": null}"),
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertTrue(validationErrorData.get("parameterName").asText()
                .contains(TEST_VALIDATION_METHOD.getName()));
        String validationErrorMessage = validationErrorData.get("message")
                .asText();
        assertTrue(validationErrorMessage
                .contains(TEST_VALIDATION_METHOD.getName()));
        assertTrue(validationErrorMessage
                .contains(TEST_ENDPOINT.getClass().toString()));
        assertTrue(validationErrorMessage.contains("null"));
    }

    @Test
    public void should_ReturnValidationError_When_EndpointMethodBeanIsInvalid()
            throws IOException {
        int invalidPropertyValue = 5;
        String propertyName = "count";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_VALIDATION_METHOD.getName());

        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME,
                        TEST_VALIDATION_METHOD.getName(),
                        createRequestParameters(String.format(
                                "{\"parameter\": {\"count\": %d}}",
                                invalidPropertyValue)),
                        requestMock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertTrue(validationErrorData.get("parameterName").asText()
                .contains(propertyName));
        String validationErrorMessage = validationErrorData.get("message")
                .asText();
        assertTrue(validationErrorMessage.contains(propertyName));
        assertTrue(validationErrorMessage
                .contains(Integer.toString(invalidPropertyValue)));
        assertTrue(validationErrorMessage.contains(
                TEST_VALIDATION_METHOD.getParameterTypes()[0].toString()));
    }

    @Test
    public void should_Invoke_ExplicitNullableTypeChecker()
            throws NoSuchMethodException {
        ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
                ExplicitNullableTypeChecker.class);

        when(explicitNullableTypeChecker.checkValueForType(
                eq(NullCheckerTestClass.OK_RESPONSE), eq(String.class),
                eq(false))).thenReturn(null);

        String testOkMethod = "testOkMethod";
        ResponseEntity<String> response = createVaadinController(
                new NullCheckerTestClass(), null, null, null,
                explicitNullableTypeChecker, null).serveEndpoint(
                        NullCheckerTestClass.class.getSimpleName(),
                        testOkMethod, createRequestParameters("{}"),
                        requestMock);

        verify(explicitNullableTypeChecker).checkValueForAnnotatedElement(
                NullCheckerTestClass.OK_RESPONSE,
                NullCheckerTestClass.class.getMethod(testOkMethod), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"" + NullCheckerTestClass.OK_RESPONSE + "\"",
                response.getBody());
    }

    @Test
    public void should_ReturnException_When_ExplicitNullableTypeChecker_ReturnsError()
            throws IOException, NoSuchMethodException {
        final String errorMessage = "Got null";

        ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
                ExplicitNullableTypeChecker.class);
        String testNullMethodName = "testNullMethod";
        Method testNullMethod = NullCheckerTestClass.class
                .getMethod(testNullMethodName);
        when(explicitNullableTypeChecker.checkValueForAnnotatedElement(null,
                testNullMethod, false)).thenReturn(errorMessage);

        ResponseEntity<String> response = createVaadinController(
                new NullCheckerTestClass(), null, null, null,
                explicitNullableTypeChecker, null).serveEndpoint(
                        NullCheckerTestClass.class.getSimpleName(),
                        testNullMethodName, createRequestParameters("{}"),
                        requestMock);

        verify(explicitNullableTypeChecker).checkValueForAnnotatedElement(null,
                testNullMethod, false);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        final String message = jsonNodes.get("message").asText();
        assertTrue(message.contains("Unexpected return value"));
        assertTrue(
                message.contains(NullCheckerTestClass.class.getSimpleName()));
        assertTrue(message.contains(testNullMethodName));
        assertTrue(message.contains(errorMessage));
    }

    @Test
    public void should_ReturnResult_When_CallingSuperClassMethodWithGenericTypedParameter() {
        ResponseEntity<?> response = createVaadinController(
                new PersonEndpoint())
                        .serveEndpoint(PersonEndpoint.class.getSimpleName(),
                                "update",
                                createRequestParameters(
                                        "{\"entity\":{\"name\":\"aa\"}}"),
                                requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"name\":\"aa\"}", response.getBody());
    }

    @Test
    public void should_AllowAccessToPackagePrivateEndpoint_PublicMethods()
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException {
        Class packagePrivateEndpoint = Class.forName(
                "dev.hilla.generator.endpoints.packageprivate.PackagePrivateEndpoint");
        Constructor packagePrivateEndpointConstructor = packagePrivateEndpoint
                .getConstructor();
        packagePrivateEndpointConstructor.setAccessible(true);

        ResponseEntity<?> response = createVaadinController(
                packagePrivateEndpointConstructor.newInstance()).serveEndpoint(
                        "PackagePrivateEndpoint", "getRequest",
                        createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"Hello\"", response.getBody());
    }

    @Test
    public void should_ConvertIterableIntoArray() {
        ResponseEntity<?> response = createVaadinController(
                new IterableEndpoint()).serveEndpoint("IterableEndpoint",
                        "getFoos", createRequestParameters("{}"), requestMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[{\"bar\":\"bar\"},{\"bar\":\"bar\"}]",
                response.getBody());
    }

    @Test
    public void should_fail_When_not_having_token_in_cookies_but_have_token_in_request_header()
            throws Exception {
        createNullTokenCookies();
        assertFailsCsrf("testMethod", 1);
    }

    @Test
    public void should_fail_When_not_having_token_in_cookies_but_have_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        createNullTokenCookies();
        assertFailsCsrf("testAnonymousMethod", null);
    }

    @Test
    public void should_fail_When_not_having_token_in_headerRequest()
            throws Exception {
        createNullTokenContextInHeaderRequest();
        assertFailsCsrf("testMethod", 1);
    }

    @Test
    public void should_fail_When_not_having_cookies_And_not_having_token_in_request_header()
            throws Exception {
        createNullCookies();
        createNullTokenContextInHeaderRequest();
        assertFailsCsrf("getUserName", null);
    }

    @Test
    public void should_fail_When_not_having_cookies_And_not_having_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        createNullCookies();
        createNullTokenContextInHeaderRequest();
        assertFailsCsrf("testAnonymousMethod", null);
    }

    @Test
    public void should_fail_When_having_different_token_between_cookie_and_headerRequest_and_NoAuthentication_AnonymousAllowed()
            throws Exception {
        createAnonymousContext();
        createDifferentCookieToken();
        assertFailsCsrf("testAnonymousMethod", null);
    }

    @Test
    public void should_fail_When_having_different_token_between_cookie_and_headerRequest()
            throws Exception {
        createDifferentCookieToken();
        assertFailsCsrf("testMethod", 1);
    }

    private void createDifferentCookieToken() {
        when(requestMock.getCookies()).thenReturn(new Cookie[] {
                new Cookie(ApplicationConstants.CSRF_TOKEN, "Fusion token") });
    }

    private void createAnonymousContext() {
        when(requestMock.getUserPrincipal()).thenReturn(null);
    }

    private void createNullCookies() {
        when(requestMock.getCookies()).thenReturn(null);
    }

    private void createNullTokenCookies() {
        when(requestMock.getCookies())
                .thenReturn(new Cookie[] { new Cookie("JSESSIONID", "0") });
    }

    private void createNullTokenContextInHeaderRequest() {
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn(null);
    }

    private void assertFailsCsrf(String methodName, Object parameter)
            throws Exception {
        String expectedOutput = "{\"message\":\"Access denied\"}";

        ObjectNode body = null;
        if (parameter != null) {
            body = createRequestParameters("{\"value1\": " + parameter + "}");
        }
        ResponseEntity<String> response = createVaadinController(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, methodName, body,
                        requestMock);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(expectedOutput, response.getBody());
    }

    private void assertEndpointInfoPresent(String responseBody) {
        assertTrue(String.format(
                "Response body '%s' should have endpoint information in it",
                responseBody), responseBody.contains(TEST_ENDPOINT_NAME));
        assertTrue(String.format(
                "Response body '%s' should have endpoint information in it",
                responseBody), responseBody.contains(TEST_METHOD.getName()));
    }

    private ObjectNode createRequestParameters(String jsonBody) {
        try {
            return new ObjectMapper().readValue(jsonBody, ObjectNode.class);
        } catch (IOException e) {
            throw new AssertionError(String
                    .format("Failed to deserialize the json: %s", jsonBody), e);
        }
    }

    private <T> EndpointController createVaadinController(T endpoint) {
        return createVaadinController(endpoint, null, null, null, null, null);
    }

    private <T> EndpointController createVaadinController(T endpoint,
            JacksonObjectMapperFactory endpointMapperFactory) {
        return createVaadinController(endpoint, endpointMapperFactory, null,
                null, null, null);
    }

    private <T> EndpointController createVaadinController(T endpoint,
            EndpointAccessChecker accessChecker) {
        return createVaadinController(endpoint, null, accessChecker, null, null,
                null);
    }

    private <T> EndpointController createVaadinController(T endpoint,
            JacksonObjectMapperFactory endpointMapperFactory,
            EndpointAccessChecker accessChecker,
            EndpointNameChecker endpointNameChecker,
            ExplicitNullableTypeChecker explicitNullableTypeChecker,
            CsrfChecker csrfChecker) {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(servletContext.getAttribute(Lookup.class.getName()))
                .thenReturn(lookup);

        if (endpointMapperFactory == null) {
            endpointMapperFactory = new JacksonObjectMapperFactory.Json();
        }

        if (accessChecker == null) {
            accessChecker = mock(EndpointAccessChecker.class);
            when(accessChecker.check(TEST_METHOD, requestMock))
                    .thenReturn(null);
        }
        if (csrfChecker == null) {
            csrfChecker = new CsrfChecker(servletContext);
        }

        if (endpointNameChecker == null) {
            endpointNameChecker = mock(EndpointNameChecker.class);
            when(endpointNameChecker.check(TEST_ENDPOINT_NAME))
                    .thenReturn(null);
        }

        if (explicitNullableTypeChecker == null) {
            explicitNullableTypeChecker = mock(
                    ExplicitNullableTypeChecker.class);
            when(explicitNullableTypeChecker.checkValueForType(any(), any(),
                    ArgumentMatchers.anyBoolean())).thenReturn(null);
        }

        ApplicationContext mockApplicationContext = mockApplicationContext(
                endpoint);
        EndpointRegistry registry = new EndpointRegistry(endpointNameChecker);

        EndpointInvoker invoker = Mockito
                .spy(new EndpointInvoker(mockApplicationContext,
                        endpointMapperFactory, explicitNullableTypeChecker,
                        mock(ServletContext.class), registry));

        Mockito.doReturn(accessChecker).when(invoker).getAccessChecker();

        EndpointController connectController = Mockito
                .spy(new EndpointController(mockApplicationContext, registry,
                        invoker, csrfChecker));
        return connectController;
    }

    private EndpointController createVaadinControllerWithoutPrincipal() {
        when(requestMock.getUserPrincipal()).thenReturn(null);
        return createVaadinController(TEST_ENDPOINT,
                new EndpointAccessChecker(new AccessAnnotationChecker()));
    }

    private EndpointController createVaadinControllerWithApplicationContext(
            ApplicationContext applicationContext) {
        EndpointControllerMockBuilder controllerMockBuilder = new EndpointControllerMockBuilder();
        EndpointController hillaController = controllerMockBuilder
                .withObjectMapperFactory(new JacksonObjectMapperFactory.Json())
                .withApplicationContext(applicationContext).build();
        return hillaController;
    }

    private Method createEndpointMethodMockThatThrows(Object argument,
            Exception exceptionToThrow) throws Exception {
        Method endpointMethodMock = mock(Method.class);
        when(endpointMethodMock.invoke(TEST_ENDPOINT, argument))
                .thenThrow(exceptionToThrow);
        when(endpointMethodMock.getParameters())
                .thenReturn(TEST_METHOD.getParameters());
        doReturn(TEST_METHOD.getDeclaringClass()).when(endpointMethodMock)
                .getDeclaringClass();
        when(endpointMethodMock.getParameterTypes())
                .thenReturn(TEST_METHOD.getParameterTypes());
        when(endpointMethodMock.getName()).thenReturn(TEST_METHOD.getName());
        return endpointMethodMock;
    }

    private ServletContext mockServletContext() {
        ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(
                context.getAttribute(ApplicationConfiguration.class.getName()))
                .thenReturn(appConfig);
        return context;
    }

    private <T> ApplicationContext mockApplicationContext(T endpoint) {
        Class<?> endpointClass = endpoint.getClass();

        ApplicationContext contextMock = mock(ApplicationContext.class);
        when(contextMock.getBeansWithAnnotation(Endpoint.class)).thenReturn(
                Collections.singletonMap(endpointClass.getName(), endpoint));
        return contextMock;
    }

}
