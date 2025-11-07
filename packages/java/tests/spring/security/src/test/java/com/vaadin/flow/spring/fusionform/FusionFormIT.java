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
package com.vaadin.flow.spring.fusionform;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class FusionFormIT extends ChromeBrowserTest {

    private static final int SERVER_PORT = 9999;

    @Override
    protected int getDeploymentPort() {
        return SERVER_PORT;
    }

    private void open(String path) {
        getDriver().get(getRootURL() + "/" + path);
    }

    @Override
    public void setup() throws Exception {
        super.setup();
        open("form");
        assertPathShown("form");
        waitUntil(driver -> $("vaadin-elements-view").exists());
    }

    @After
    public void tearDown() {
        if (getDriver() != null) {
            checkLogsForErrors(msg -> {
                // TODO: Remove when
                // https://github.com/vaadin/web-components/issues/8889 is fixed
                return msg.contains(".slotStyles is not iterable")
                        // form validation errors
                        || msg.contains(
                                "the server responded with a status of 400 ()");
            });
        }
    }

    @Test
    public void save_empty_values_for_required_fields_no_runtime_errors() {
        ButtonElement saveButton = $(ButtonElement.class).id("save");
        saveButton.click();
        NotificationElement notification = $(NotificationElement.class)
                .id("notification");
        Assert.assertNotNull(notification);
        waitUntil(driver -> notification.isOpen());
        Assert.assertTrue(notification.getText().contains("must not be empty"));
        Assert.assertFalse(notification.getText()
                .contains("Expected string but received a undefined"));
    }

    @Test
    public void save_backend_loaded_empty_values_for_required_fields_no_runtime_errors() {
        ButtonElement loadDataButton = $(ButtonElement.class)
                .id("load-from-endpoint");
        loadDataButton.click();
        ButtonElement saveButton = $(ButtonElement.class).id("save");
        waitUntil(driver -> saveButton.isEnabled());
        saveButton.click();
        NotificationElement notification = $(NotificationElement.class)
                .id("notification");
        Assert.assertNotNull(notification);
        waitUntil(driver -> notification.isOpen());
        Assert.assertTrue(notification.getText().contains("must not be empty"));
        Assert.assertFalse(notification.getText()
                .contains("Expected string but received null"));
    }

    @Test
    // https://github.com/vaadin/fusion/issues/13
    public void no_validation_error_when_clearing_number_field() {
        NumberFieldElement numberFieldElement = $(NumberFieldElement.class)
                .id("number-field");
        numberFieldElement.setValue("5");
        blur();
        Assert.assertFalse(numberFieldElement.hasAttribute("invalid"));
        Assert.assertFalse(
                numberFieldElement.hasAttribute("has-error-message"));
        numberFieldElement.setValue("");
        blur();
        Assert.assertFalse(numberFieldElement.hasAttribute("invalid"));
        Assert.assertFalse(
                numberFieldElement.hasAttribute("has-error-message"));
    }

    private void assertPathShown(String path) {
        waitUntil(driver -> driver.getCurrentUrl()
                .equals(getRootURL() + "/" + path));
    }

}
