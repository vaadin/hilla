package com.vaadin.flow.spring.fusionform;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
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
                // form validation errors
                return msg.contains("the server responded with a status of 400 ()");
            });
        }
    }

    @Test
    public void save_empty_values_for_required_fields_no_runtime_errors() {
        ButtonElement saveButton = $(ButtonElement.class).id("save");
        saveButton.click();
        NotificationElement notification = $(NotificationElement.class).id("notification");
        Assert.assertNotNull(notification);
        waitUntil(driver -> notification.isOpen());
        Assert.assertTrue(notification.getText().contains("must not be empty"));
        Assert.assertFalse(notification.getText().contains("Expected string but received a undefined"));
    }

    @Test
    public void save_backend_loaded_empty_values_for_required_fields_no_runtime_errors() {
        ButtonElement loadDataButton = $(ButtonElement.class).id("load-from-endpoint");
        loadDataButton.click();
        ButtonElement saveButton = $(ButtonElement.class).id("save");
        waitUntil(driver -> saveButton.isEnabled());
        saveButton.click();
        NotificationElement notification = $(NotificationElement.class).id("notification");
        Assert.assertNotNull(notification);
        waitUntil(driver -> notification.isOpen());
        Assert.assertTrue(notification.getText().contains("must not be empty"));
        Assert.assertFalse(notification.getText().contains("Expected string but received null"));
    }

    @Test
    // https://github.com/vaadin/fusion/issues/13
    public void no_validation_error_when_clearing_number_field() {
        NumberFieldElement numberFieldElement = $(NumberFieldElement.class).id("number-field");
        numberFieldElement.setValue("5");
        blur();
        Assert.assertFalse(numberFieldElement.hasAttribute("invalid"));
        Assert.assertFalse(numberFieldElement.hasAttribute("has-error-message"));
        numberFieldElement.setValue("");
        blur();
        Assert.assertFalse(numberFieldElement.hasAttribute("invalid"));
        Assert.assertFalse(numberFieldElement.hasAttribute("has-error-message"));
    }

    private void assertPathShown(String path) {
        waitUntil(driver -> driver.getCurrentUrl()
                .equals(getRootURL() + "/" + path));
    }

}
