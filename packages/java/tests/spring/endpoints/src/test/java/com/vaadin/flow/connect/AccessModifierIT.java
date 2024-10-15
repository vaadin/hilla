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
import com.vaadin.testbench.TestBenchElement;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class AccessModifierIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testAccessMod;
    private TestBenchElement methods;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open();
        TestBenchElement testComponent = $("test-component").waitForFirst();
        if (testComponent != null) {
            testComponent.$(TestBenchElement.class).id("username")
                    .sendKeys("user");
            testComponent.$(TestBenchElement.class).id("password")
                    .sendKeys("user");
            testComponent.$(TestBenchElement.class).id("login").click();
            open();
        }
        testAccessMod = $("test-access-mod").waitForFirst();
        methods = testAccessMod.$(TestBenchElement.class).id("methods");
    }

    @Override
    protected void open() {
        openTestUrl("/access-mod");
    }

    @Test
    public void getEntity() {
        String endpoint = "getEntity";
        exec(endpoint);
        String actualText = waitUntil(driver -> methods.getText(), 25);
        Assert.assertNotNull(actualText);
        Assert.assertTrue(actualText.contains("publicProp"));
        Assert.assertFalse(actualText.contains("protectedProp"));
        Assert.assertFalse(actualText.contains("packagePrivateProp"));
        Assert.assertFalse(actualText.contains("privateProp"));
        Assert.assertTrue(actualText.contains("publicGetterProp"));
        // Assert.assertTrue(actualText.contains("publicSetterProp")); //
        // property is public, but not present in prototype
    }

    private void exec(String id) {
        methods.setProperty("innerText", "");
        WebElement button = testAccessMod.$(TestBenchElement.class).id(id);
        button.click();
    }
}
