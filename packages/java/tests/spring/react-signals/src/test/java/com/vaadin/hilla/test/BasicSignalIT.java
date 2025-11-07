/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.test;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

// Tests are disabled due to unstable signal implementation.
// Re-enable when there is a new signal implementation.
@Ignore
public class BasicSignalIT extends ChromeBrowserTest {
    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/BasicSignalView");
    }

    @Test
    public void shouldEchoInput() {
        $(TextFieldElement.class).waitForFirst().setValue("John Doe");
        waitUntil(
                textToBePresentInElement($("span").first(), "Echo: John Doe"));
    }
}
