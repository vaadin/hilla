/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
