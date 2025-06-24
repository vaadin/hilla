/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.typeconversion;

import com.vaadin.hilla.EndpointController;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vaadin.flow.server.startup.ApplicationConfiguration;

import java.io.IOException;

@RunWith(SpringRunner.class)
@WebMvcTest
@Import(TestTypeConversionEndpoints.class)
public abstract class BaseTypeConversionTest {

    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    private ApplicationConfiguration appConfig;

    @Rule
    public TemporaryFolder projectFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Assert.assertNotNull(applicationContext);
        projectFolder.newFolder("build");

        appConfig = Mockito.mock(ApplicationConfiguration.class);
        Mockito.when(appConfig.isProductionMode()).thenReturn(false);
        Mockito.when(appConfig.getProjectFolder())
                .thenReturn(projectFolder.getRoot());
        Mockito.when(appConfig.getBuildFolder()).thenReturn("build");

        EndpointControllerMockBuilder controllerMockBuilder = new EndpointControllerMockBuilder();
        EndpointController controller = controllerMockBuilder
                .withApplicationContext(applicationContext).build();
        controller.registerEndpoints();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    protected void assertEqualExpectedValueWhenCallingMethod(String methodName,
            String requestValue, String expectedValue) {
        try {
            MockHttpServletResponse response = callMethod(methodName,
                    requestValue);
            Assert.assertEquals(expectedValue, response.getContentAsString());
            Assert.assertEquals(200, response.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assert400ResponseWhenCallingMethod(String methodName,
            String requestValue) {
        try {
            MockHttpServletResponse response = callMethod(methodName,
                    requestValue);
            Assert.assertEquals(400, response.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected MockHttpServletResponse callMethod(String methodName,
            String requestValue) throws Exception {
        String endpointName = TestTypeConversionEndpoints.class.getSimpleName();
        String requestUrl = String.format("/%s/%s", endpointName, methodName);
        String body = String.format("{\"value\": %s}", requestValue);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(requestUrl)
                .accept(MediaType.APPLICATION_JSON_VALUE).content(body)
                .contentType(MediaType.APPLICATION_JSON_VALUE);
        return mockMvc.perform(requestBuilder).andReturn().getResponse();
    }

}
