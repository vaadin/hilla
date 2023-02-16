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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class TypeScriptIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testTypeScript;
    private TestBenchElement content;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open();
        testTypeScript = $("test-type-script").waitForFirst();
        content = testTypeScript.$(TestBenchElement.class).id("content");
    }

    @Override
    protected void open() {
        openTestUrl("/type-script");
    }

    @Test
    public void annotatedEntity() {
        String endpoint = "getAnnotatedEntity";
        exec(endpoint);
        assertContent("value");
    }

    @Test
    public void annotatedEntityModelType() {
        String endpoint = "checkAnnotatedEntityModelType";
        exec(endpoint);
        assertContent("string");
    }

    private void exec(String id) {
        content.setProperty("innerText", "");
        WebElement button = testTypeScript.$(TestBenchElement.class).id(id);
        button.click();
    }

    private void assertContent(String expected) {
        waitUntil(driver -> {
            return content.getText().equals(expected);
        }, 25);
    }
}
