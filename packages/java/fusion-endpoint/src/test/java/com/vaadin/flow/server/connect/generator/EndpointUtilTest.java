package com.vaadin.flow.server.connect.generator;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.connect.EndpointUtil;
import com.vaadin.flow.server.connect.VaadinEndpointProperties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { EndpointUtil.class,
        VaadinEndpointProperties.class })
@RunWith(SpringRunner.class)
public class EndpointUtilTest {

    @Autowired
    private EndpointUtil endpointUtil;

    @Test
    public void endpointRequest() {
        testPath("/connect/hello/world", true);
        testPath("/connect/bar/baz", true);
    }

    @Test
    public void nonEndpointRequest() {
        testPath("/", false);
        testPath("/VAADIN", false);
        testPath("/vaadinServlet", false);
    }

    private void testPath(String path, boolean expected) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn(path);
        Assert.assertEquals(expected, endpointUtil.isEndpointRequest(request));

        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServletPath()).thenReturn(path);
        Assert.assertEquals(expected, endpointUtil.isEndpointRequest(request));
    }
}
