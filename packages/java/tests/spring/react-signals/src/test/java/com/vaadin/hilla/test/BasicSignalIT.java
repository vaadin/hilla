package com.vaadin.hilla.test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

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
