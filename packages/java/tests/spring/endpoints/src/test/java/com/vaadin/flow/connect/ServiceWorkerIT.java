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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.flow.testutil.ChromeDeviceTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

/**
 * Class for testing issues in a spring-boot container.
 */
public class ServiceWorkerIT extends ChromeDeviceTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testComponent;
    private WebElement content;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        load();
    }

    @Test
    public void should_requestAnonymously_when_calledInServiceWorker() {
        assumeThat("Service workers require secure context deployment", this.getDeploymentHostname(), is("localhost"));

        WebElement button = testComponent.$(TestBenchElement.class)
            .id("helloAnonymousFromServiceWorker");
        button.click();

        // Wait for the server connect response
        verifyContent( "SW message: Hello, stranger!");
    }

    private void load() {
        openTestUrl("/");
        waitForServiceWorkerReady();
        testComponent = $("test-component").waitForFirst();
        content = testComponent.$(TestBenchElement.class).id("content");
    }

    private void verifyContent(String expected) {
        waitUntil(
                ExpectedConditions.textToBePresentInElement(content, expected),
                25);
    }
}
