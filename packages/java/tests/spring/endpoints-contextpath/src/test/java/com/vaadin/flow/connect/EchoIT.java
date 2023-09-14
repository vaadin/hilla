/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.connect;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class EchoIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/context");
    }

    @Test
    public void normalEndpointWorks() {
        setInput("normal");
        getTestView().$("button").id("normalEndpoint").click();
        assertResponse("normal");
    }

    @Test
    public void fluxEndpointWorks() {
        setInput("normal");
        getTestView().$("button").id("fluxEndpoint").click();
        assertResponse("normal");
    }

    private void assertResponse(String response) {
        waitUntil(driver -> {
            return getTestView().$("*").id("result").getText().equals(response);
        });

    }

    private void setInput(String value) {
        getTestView().$("*").id("input").setProperty("value", value);

    }

    private TestBenchElement getTestView() {
        return $("test-view").first();
    }

}
