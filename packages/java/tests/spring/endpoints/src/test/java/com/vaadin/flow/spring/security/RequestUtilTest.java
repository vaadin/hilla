package com.vaadin.flow.spring.security;

import jakarta.annotation.security.RolesAllowed;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hilla.EndpointController;
import dev.hilla.EndpointControllerConfiguration;
import dev.hilla.EndpointProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.internal.NavigationRouteTarget;
import com.vaadin.flow.router.internal.RouteTarget;
import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringSecurityAutoConfiguration;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.SpringVaadinServletService;
import com.vaadin.flow.spring.VaadinConfigurationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { EndpointProperties.class })
@ContextConfiguration(classes = { EndpointControllerConfiguration.class,
        SpringBootAutoConfiguration.class,
        SpringSecurityAutoConfiguration.class, ObjectMapper.class,
        Jackson2ObjectMapperBuilder.class, JacksonProperties.class,
        EndpointController.class })
public class RequestUtilTest {

    @Autowired
    RequestUtil requestUtil;

    @MockBean
    VaadinConfigurationProperties vaadinConfigurationProperties;

    @MockBean
    private ServletRegistrationBean<SpringServlet> springServletRegistration;

    @Test
    public void testRootRequest_init_standardMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");

        Assert.assertTrue(requestUtil.isFrameworkInternalRequest(
                createRequest("", RequestType.INIT)));
    }

    @Test
    public void testRootRequest_other_standardMapping() {
        // given(this.vaadinConfigurationProperties.getUrlMapping()).willReturn("Hello");
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        Assert.assertFalse(requestUtil
                .isFrameworkInternalRequest(createRequest("", null)));
    }

    @Test
    public void testSubRequest_init_standardMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(
                createRequest("/foo", RequestType.INIT)));
    }

    @Test
    public void testSubRequest_other_standardMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        Assert.assertFalse(requestUtil
                .isFrameworkInternalRequest(createRequest("/foo", null)));
    }

    @Test
    public void testRootRequest_init_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        MockHttpServletRequest request = createRequest("/", RequestType.INIT);
        request.setServletPath("/bar");
        Assert.assertTrue(requestUtil.isFrameworkInternalRequest(request));

        request = createRequest("", RequestType.INIT);
        request.setServletPath("/bar");
        Assert.assertTrue(requestUtil.isFrameworkInternalRequest(request));

        request = createRequest(null, RequestType.INIT);
        request.setServletPath("/bar");
        Assert.assertTrue(requestUtil.isFrameworkInternalRequest(request));

    }

    @Test
    public void testRootRequest_other_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        MockHttpServletRequest request = createRequest("/", null);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(request));

        request = createRequest("", null);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(request));

        request = createRequest(null, null);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(request));
    }

    @Test
    public void testSubRequest_init_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        MockHttpServletRequest request = createRequest("/foo",
                RequestType.INIT);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(request));
    }

    @Test
    public void testExternalRequest_init_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(
                createRequest("/foo", RequestType.INIT)));
    }

    @Test
    public void testSubRequest_other_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        MockHttpServletRequest request = createRequest("/foo", null);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isFrameworkInternalRequest(request));
    }

    @Test
    public void testExternalRequest_other_customMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/bar/*");
        Assert.assertFalse(requestUtil
                .isFrameworkInternalRequest(createRequest("/foo", null)));
    }

    @Route("")
    @AnonymousAllowed
    public static class PublicRootView extends Component {

    }

    @Route("other")
    @AnonymousAllowed
    public static class AnotherPublicView extends Component {

    }

    @Route("admin")
    @RolesAllowed("admin")
    public static class AdminView extends Component {

    }

    @Test
    public void testAnonymousRouteRequest_rootMappedServlet_publicView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        SpringServlet servlet = setupMockServlet();
        addRoute(servlet, PublicRootView.class);
        addRoute(servlet, AnotherPublicView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));

        request = createRequest("other");
        request.setServletPath("/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_rootMappedServlet_notAView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        SpringServlet servlet = setupMockServlet();
        addRoute(servlet, PublicRootView.class);
        addRoute(servlet, AnotherPublicView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));

        request = createRequest("other");
        request.setServletPath("/bar");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_rootMappedServlet_privateView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        addRoute(setupMockServlet(), AdminView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/admin");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_fooMappedServlet_publicView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/foo/*");
        SpringServlet servlet = setupMockServlet();
        addRoute(servlet, PublicRootView.class);
        addRoute(servlet, AnotherPublicView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/foo/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));

        request = createRequest("");
        request.setServletPath("/foo/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));

        request = createRequest("/");
        request.setServletPath("/foo/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));

        request = createRequest("other");
        request.setServletPath("/foo/");
        Assert.assertTrue(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_fooMappedServlet_notAView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/foo/*");
        SpringServlet servlet = setupMockServlet();
        addRoute(servlet, PublicRootView.class);
        addRoute(servlet, AnotherPublicView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/foo/bar");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));

        request = createRequest("other");
        request.setServletPath("/foo/bar");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_fooMappedServlet_privateView() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/foo/*");
        addRoute(setupMockServlet(), AdminView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/foo/admin");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testAnonymousRouteRequest_fooMappedServlet_publicViewPathOutsideServlet() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/foo/*");
        SpringServlet servlet = setupMockServlet();
        addRoute(servlet, PublicRootView.class);
        addRoute(servlet, AnotherPublicView.class);

        MockHttpServletRequest request = createRequest(null);
        request.setServletPath("/");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
        request = createRequest("other");
        request.setServletPath("/");
        Assert.assertFalse(requestUtil.isAnonymousRoute(request));
    }

    @Test
    public void testApplyUrlMapping_fooMappedServlet_prependMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/foo/*");
        Assert.assertEquals("/foo/bar", requestUtil.applyUrlMapping("bar"));
        Assert.assertEquals("/foo/bar", requestUtil.applyUrlMapping("/bar"));
        Assert.assertEquals("/foo/bar/", requestUtil.applyUrlMapping("bar/"));
        Assert.assertEquals("/foo/bar/", requestUtil.applyUrlMapping("/bar/"));
        Assert.assertEquals("/foo/bar/baz",
                requestUtil.applyUrlMapping("bar/baz"));
        Assert.assertEquals("/foo/bar/baz",
                requestUtil.applyUrlMapping("/bar/baz"));
        Assert.assertEquals("/foo/", requestUtil.applyUrlMapping(""));
        Assert.assertEquals("/foo/", requestUtil.applyUrlMapping("/"));
        Assert.assertEquals("/foo/", requestUtil.applyUrlMapping(null));
    }

    @Test
    public void testApplyUrlMapping_rootMappedServlet_prependMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn("/*");
        Assert.assertEquals("/bar", requestUtil.applyUrlMapping("bar"));
        Assert.assertEquals("/bar", requestUtil.applyUrlMapping("/bar"));
        Assert.assertEquals("/bar/", requestUtil.applyUrlMapping("bar/"));
        Assert.assertEquals("/bar/", requestUtil.applyUrlMapping("/bar/"));
        Assert.assertEquals("/bar/baz", requestUtil.applyUrlMapping("bar/baz"));
        Assert.assertEquals("/bar/baz",
                requestUtil.applyUrlMapping("/bar/baz"));
        Assert.assertEquals("/", requestUtil.applyUrlMapping(""));
        Assert.assertEquals("/", requestUtil.applyUrlMapping("/"));
        Assert.assertEquals("/", requestUtil.applyUrlMapping(null));
    }

    @Test
    public void testApplyUrlMapping_nullMappedServlet_prependMapping() {
        Mockito.when(vaadinConfigurationProperties.getUrlMapping())
                .thenReturn(null);
        Assert.assertEquals("/bar", requestUtil.applyUrlMapping("bar"));
        Assert.assertEquals("/bar", requestUtil.applyUrlMapping("/bar"));
        Assert.assertEquals("/bar/", requestUtil.applyUrlMapping("bar/"));
        Assert.assertEquals("/bar/", requestUtil.applyUrlMapping("/bar/"));
        Assert.assertEquals("/bar/baz", requestUtil.applyUrlMapping("bar/baz"));
        Assert.assertEquals("/bar/baz",
                requestUtil.applyUrlMapping("/bar/baz"));
        Assert.assertEquals("/", requestUtil.applyUrlMapping(""));
        Assert.assertEquals("/", requestUtil.applyUrlMapping("/"));
        Assert.assertEquals("/", requestUtil.applyUrlMapping(null));
    }

    private SpringServlet setupMockServlet() {
        SpringServlet servlet = Mockito.mock(SpringServlet.class);
        SpringVaadinServletService service = Mockito
                .mock(SpringVaadinServletService.class);
        Router router = Mockito.mock(Router.class);
        RouteRegistry routeRegistry = Mockito.mock(RouteRegistry.class);

        Mockito.when(springServletRegistration.getServlet())
                .thenReturn(servlet);
        Mockito.when(servlet.getService()).thenReturn(service);
        Mockito.when(service.getRouter()).thenReturn(router);
        Mockito.when(router.getRegistry()).thenReturn(routeRegistry);
        return servlet;
    }

    private void addRoute(SpringServlet servlet,
            Class<? extends Component> view) {
        Optional<Route> route = AnnotationReader.getAnnotationFor(view,
                Route.class);
        if (!route.isPresent()) {
            throw new IllegalArgumentException(
                    "Unable find a @Route annotation");
        }

        String path = route.get().value();
        RouteRegistry routeRegistry = servlet.getService().getRouter()
                .getRegistry();
        RouteTarget publicRouteTarget = Mockito.mock(RouteTarget.class);
        NavigationRouteTarget navigationTarget = Mockito
                .mock(NavigationRouteTarget.class);

        Mockito.when(routeRegistry.getNavigationRouteTarget(path))
                .thenReturn(navigationTarget);
        Mockito.when(navigationTarget.getRouteTarget())
                .thenReturn(publicRouteTarget);
        Mockito.when(publicRouteTarget.getTarget()).thenReturn((Class) view);

    }

    static MockHttpServletRequest createRequest(String pathInfo) {
        return createRequest(pathInfo, null);
    }

    static MockHttpServletRequest createRequest(String pathInfo,
            RequestType type) {
        return createRequest(pathInfo, type, Collections.emptyMap());
    }

    static MockHttpServletRequest createRequest(String pathInfo,
            RequestType type, Map<String, String> headers) {
        String uri = (pathInfo == null ? "/" : pathInfo);
        MockHttpServletRequest r = new MockHttpServletRequest("GET", uri);
        r.setPathInfo(pathInfo);
        if (type != null) {
            r.setParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER,
                    type.getIdentifier());
        }
        headers.forEach((key, value) -> r.addHeader(key, value));

        return r;
    }

}
